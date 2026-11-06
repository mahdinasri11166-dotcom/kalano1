package com.example.anbar

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.google.android.material.floatingactionbutton.FloatingActionButton

/** فهرست کالاها با موجودی، جستجو و ویرایش سریع */
class ProductsListActivity : AppCompatActivity() {
    private lateinit var repo: InventoryRepository
    private lateinit var box: LinearLayout
    private var all: List<Product> = emptyList()
    private var stockMap: Map<Long, Double> = emptyMap()

    override fun onCreate(savedInstanceState: Bundle?) {
        LocaleHelper.applyLocaleToActivity(this)
        super.onCreate(savedInstanceState)
        repo = InventoryRepository(this)
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(12, 12, 12, 12)
            setBackgroundColor(0xFFF1F5F9.toInt())
        }
        root.addView(TextView(this).apply {
            text = "کالاها"
            textSize = 20f
            setTypeface(null, android.graphics.Typeface.BOLD)
        })
        val et = EditText(this).apply {
            hint = "جستجو نام / کد / بارکد"
            setPadding(24, 20, 24, 20)
        }
        et.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) { render(s?.toString().orEmpty()) }
        })
        root.addView(et)
        box = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        root.addView(ScrollView(this).apply {
            layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 0, 1f)
            addView(box)
        })
        root.addView(MaterialButton(this).apply {
            text = "+ کالای جدید"
            setOnClickListener { startActivity(Intent(this@ProductsListActivity, AddProductActivity::class.java)) }
        })
        setContentView(root)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "فهرست کالاها"
    }

    override fun onResume() {
        super.onResume()
        all = repo.getProducts()
        stockMap = repo.getAllStock().groupBy { it.productId }.mapValues { it.value.sumOf { s -> s.quantity } }
        render("")
    }

    private fun render(q: String) {
        box.removeAllViews()
        val list = if (q.isBlank()) all else all.filter {
            it.name.contains(q, true) || it.code.contains(q, true) || it.barcode.contains(q, true)
        }
        if (list.isEmpty()) {
            box.addView(TextView(this).apply {
                text = "کالایی نیست — «کالای جدید» را بزنید"
                setPadding(8, 24, 8, 8)
            })
            return
        }
        list.forEach { p ->
            val stock = stockMap[p.id] ?: 0.0
            val low = p.minStock > 0 && stock <= p.minStock || (p.minStock <= 0 && stock <= 5)
            val card = MaterialCardView(this).apply {
                radius = 14f; cardElevation = 0f; useCompatPadding = true
                setCardBackgroundColor(if (low) 0xFFFFF7ED.toInt() else Color.WHITE)
            }
            val col = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                setPadding(14, 12, 14, 12)
            }
            col.addView(TextView(this).apply {
                text = p.name
                setTypeface(null, android.graphics.Typeface.BOLD)
                textSize = 15f
            })
            col.addView(TextView(this).apply {
                text = buildString {
                    append("کد: ${p.code.ifBlank { "—" }}")
                    if (p.barcode.isNotBlank()) append(" · بارکد: ${p.barcode}")
                    append("\nموجودی: ${FormatUtils.qty(stock)} ${p.unit}")
                    if (low) append("  ⚠ کمبود")
                    append("\nخرید: ${FormatUtils.money(p.purchasePrice)} · فروش: ${FormatUtils.money(p.salePrice)}")
                    if (p.category.isNotBlank()) append(" · ${p.category}")
                }
                textSize = 12f
                setTextColor(0xFF475569.toInt())
            })
            col.addView(MaterialButton(this, null, com.google.android.material.R.attr.materialButtonOutlinedStyle).apply {
                text = "ویرایش قیمت / موجودی سریع"
                textSize = 12f
                setOnClickListener { quickEdit(p, stock) }
            })
            card.addView(col)
            box.addView(card)
        }
    }

    private fun quickEdit(p: Product, stock: Double) {
        val etSale = EditText(this).apply {
            hint = "قیمت فروش"
            inputType = android.text.InputType.TYPE_CLASS_NUMBER or android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL
            setText(if (p.salePrice > 0) p.salePrice.toLong().toString() else "")
            setPadding(40, 20, 40, 12)
        }
        val etPurchase = EditText(this).apply {
            hint = "قیمت خرید"
            inputType = android.text.InputType.TYPE_CLASS_NUMBER or android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL
            setText(if (p.purchasePrice > 0) p.purchasePrice.toLong().toString() else "")
            setPadding(40, 12, 40, 12)
        }
        val etMin = EditText(this).apply {
            hint = "حداقل موجودی"
            inputType = android.text.InputType.TYPE_CLASS_NUMBER or android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL
            setText(if (p.minStock > 0) p.minStock.toLong().toString() else "0")
            setPadding(40, 12, 40, 20)
        }
        val box = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(24, 8, 24, 0)
            addView(TextView(this@ProductsListActivity).apply {
                text = "موجودی فعلی: ${FormatUtils.qty(stock)}"
                setPadding(0, 0, 0, 8)
            })
            addView(etSale); addView(etPurchase); addView(etMin)
        }
        AlertDialog.Builder(this)
            .setTitle(p.name)
            .setView(box)
            .setPositiveButton("ذخیره") { _, _ ->
                val sale = etSale.text?.toString()?.toDoubleOrNull() ?: p.salePrice
                val purchase = etPurchase.text?.toString()?.toDoubleOrNull() ?: p.purchasePrice
                val min = etMin.text?.toString()?.toDoubleOrNull() ?: 0.0
                repo.updateProduct(p.copy(salePrice = sale, purchasePrice = purchase, minStock = min))
                if (sale > 0) {
                    try {
                        repo.setProductPrice(ProductPrice(p.id, retail = sale, wholesale = sale * 0.9, colleague = sale * 0.85))
                    } catch (_: Exception) {}
                }
                Toast.makeText(this, "ذخیره شد", Toast.LENGTH_SHORT).show()
                onResume()
            }
            .setNegativeButton("بستن", null)
            .show()
    }

    override fun onSupportNavigateUp(): Boolean { finish(); return true }
}

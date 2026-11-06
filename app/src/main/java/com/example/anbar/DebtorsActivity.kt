package com.example.anbar

import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView

/** مشتریان بدهکار — وصول سریع */
class DebtorsActivity : AppCompatActivity() {
    private lateinit var repo: InventoryRepository
    private lateinit var box: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        LocaleHelper.applyLocaleToActivity(this)
        super.onCreate(savedInstanceState)
        repo = InventoryRepository(this)
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(12, 12, 12, 12)
            setBackgroundColor(0xFFF8FAFC.toInt())
        }
        root.addView(TextView(this).apply {
            text = "بدهکاران"
            textSize = 20f
            setTypeface(null, android.graphics.Typeface.BOLD)
        })
        root.addView(TextView(this).apply {
            text = "مشتریانی که بدهی دارند — تماس یا ثبت دریافت"
            textSize = 12f
            setTextColor(0xFF64748B.toInt())
            setPadding(0, 4, 0, 12)
        })
        box = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        root.addView(ScrollView(this).apply {
            layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 0, 1f)
            addView(box)
        })
        setContentView(root)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "وصول مطالبات"
    }

    override fun onResume() {
        super.onResume()
        load()
    }

    private fun load() {
        box.removeAllViews()
        val list = repo.getCustomersWithDebt()
        if (list.isEmpty()) {
            box.addView(TextView(this).apply {
                text = "بدهکاری ثبت نشده — عالی است!"
                setPadding(8, 24, 8, 8)
                setTextColor(0xFF16A34A.toInt())
            })
            return
        }
        val total = list.sumOf { it.creditBalance }
        box.addView(TextView(this).apply {
            text = "جمع مطالبات: ${FormatUtils.money(total)} · ${list.size} نفر"
            setTypeface(null, android.graphics.Typeface.BOLD)
            setTextColor(0xFFB91C1C.toInt())
            setPadding(4, 4, 4, 12)
        })
        list.forEach { c ->
            val card = MaterialCardView(this).apply {
                radius = 14f; cardElevation = 0f; useCompatPadding = true
                setCardBackgroundColor(Color.WHITE)
            }
            val col = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                setPadding(14, 12, 14, 12)
            }
            col.addView(TextView(this).apply {
                text = c.name
                setTypeface(null, android.graphics.Typeface.BOLD)
            })
            col.addView(TextView(this).apply {
                text = "بدهی: ${FormatUtils.money(c.creditBalance)}" +
                        if (c.creditLimit > 0) " / سقف ${FormatUtils.money(c.creditLimit)}" else ""
                setTextColor(0xFFDC2626.toInt())
                textSize = 13f
            })
            if (c.phone.isNotBlank()) {
                col.addView(TextView(this).apply {
                    text = "☎ ${c.phone}"
                    textSize = 12f
                    setTextColor(0xFF64748B.toInt())
                })
            }
            val row = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
            if (c.phone.isNotBlank()) {
                row.addView(MaterialButton(this, null, com.google.android.material.R.attr.materialButtonOutlinedStyle).apply {
                    text = "تماس"
                    textSize = 12f
                    setOnClickListener {
                        try {
                            startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:${c.phone}")))
                        } catch (_: Exception) {
                            Toast.makeText(this@DebtorsActivity, "خطا در تماس", Toast.LENGTH_SHORT).show()
                        }
                    }
                })
                row.addView(MaterialButton(this, null, com.google.android.material.R.attr.materialButtonOutlinedStyle).apply {
                    text = "واتساپ"
                    textSize = 12f
                    setOnClickListener {
                        val msg = "سلام ${c.name}، مانده حساب شما نزد ${repo.getSettings().shopName}: ${FormatUtils.money(c.creditBalance)}. لطفاً جهت تسویه اقدام فرمایید."
                        try {
                            val phone = c.phone.filter { it.isDigit() }.let {
                                if (it.startsWith("0")) "98${it.drop(1)}" else it
                            }
                            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://wa.me/$phone?text=${Uri.encode(msg)}")))
                        } catch (_: Exception) {
                            Toast.makeText(this@DebtorsActivity, "واتساپ در دسترس نیست", Toast.LENGTH_SHORT).show()
                        }
                    }
                })
            }
            row.addView(MaterialButton(this).apply {
                text = "دریافت وجه"
                textSize = 12f
                setOnClickListener { receive(c) }
            })
            row.addView(MaterialButton(this, null, com.google.android.material.R.attr.materialButtonOutlinedStyle).apply {
                text = "پروفایل"
                textSize = 12f
                setOnClickListener {
                    startActivity(Intent(this@DebtorsActivity, CustomerDetailActivity::class.java).putExtra("customer_id", c.id))
                }
            })
            col.addView(row)
            card.addView(col)
            box.addView(card)
        }
    }

    private fun receive(c: Customer) {
        val et = EditText(this).apply {
            hint = "مبلغ دریافتی"
            inputType = android.text.InputType.TYPE_CLASS_NUMBER or android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL
            setText(c.creditBalance.toLong().toString())
            setPadding(40, 28, 40, 28)
        }
        AlertDialog.Builder(this)
            .setTitle("دریافت از ${c.name}")
            .setView(et)
            .setPositiveButton("ثبت") { _, _ ->
                val amount = et.text?.toString()?.toDoubleOrNull() ?: 0.0
                if (amount <= 0) {
                    Toast.makeText(this, "مبلغ نامعتبر", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }
                val newBal = (c.creditBalance - amount).coerceAtLeast(0.0)
                repo.updateCustomer(c.copy(creditBalance = newBal))
                repo.logActivity("RECEIVE", "${c.name}: ${FormatUtils.money(amount)}")
                Toast.makeText(this, "ثبت شد · مانده ${FormatUtils.money(newBal)}", Toast.LENGTH_LONG).show()
                load()
            }
            .setNegativeButton("لغو", null)
            .show()
    }

    override fun onSupportNavigateUp(): Boolean { finish(); return true }
}

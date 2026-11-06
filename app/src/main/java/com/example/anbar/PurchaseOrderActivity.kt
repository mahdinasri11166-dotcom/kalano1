package com.example.anbar

import android.content.Intent
import android.os.Bundle
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import java.io.File

/** سفارش خرید از روی پیشنهاد کمبود موجودی */
class PurchaseOrderActivity : AppCompatActivity() {
    private lateinit var repo: InventoryRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        LocaleHelper.applyLocaleToActivity(this)
        super.onCreate(savedInstanceState)
        if (!LicenseManager.canUsePurchaseOrder(this)) {
            Toast.makeText(this, LicenseManager.featureBlockedMessage("حرفه‌ای"), Toast.LENGTH_LONG).show()
            startActivity(Intent(this, LicenseActivity::class.java))
            finish()
            return
        }
        repo = InventoryRepository(this)
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(16, 16, 16, 24)
            setBackgroundColor(0xFFF8FAFC.toInt())
        }
        root.addView(TextView(this).apply {
            text = "سفارش خرید"
            textSize = 20f
            setTypeface(null, android.graphics.Typeface.BOLD)
        })
        root.addView(TextView(this).apply {
            text = "بر اساس کمبود موجودی و حداقل تعریف‌شده"
            textSize = 12f
            setTextColor(0xFF64748B.toInt())
            setPadding(0, 4, 0, 12)
        })
        val box = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        val list = repo.getReorderSuggestions()
        if (list.isEmpty()) {
            box.addView(TextView(this).apply {
                text = "الان کمبودی ثبت نشده."
                setPadding(8, 24, 8, 8)
            })
        } else {
            list.forEach { s ->
                val c = MaterialCardView(this).apply {
                    radius = 12f; useCompatPadding = true; cardElevation = 0f
                    setCardBackgroundColor(0xFFFFFFFF.toInt())
                }
                c.addView(TextView(this).apply {
                    text = "${s.productName}\nموجود: ${FormatUtils.qty(s.currentStock)} · پیشنهاد سفارش: ${FormatUtils.qty(s.suggestedQty)} ${s.unit}"
                    setPadding(14, 12, 14, 12)
                })
                box.addView(c)
            }
        }
        root.addView(ScrollView(this).apply {
            layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 0, 1f)
            addView(box)
        })
        root.addView(MaterialButton(this).apply {
            text = "اشتراک متن سفارش"
            setOnClickListener {
                if (list.isEmpty()) {
                    Toast.makeText(this@PurchaseOrderActivity, "موردی نیست", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                val shop = repo.getSettings().shopName
                val body = buildString {
                    appendLine("سفارش خرید — $shop")
                    appendLine(FormatUtils.dateTime(System.currentTimeMillis()))
                    appendLine("────────────")
                    list.forEach {
                        appendLine("• ${it.productName}: ${FormatUtils.qty(it.suggestedQty)} ${it.unit}")
                    }
                    appendLine("────────────")
                    appendLine("از کالانو")
                }
                val send = Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_TEXT, body)
                }
                startActivity(Intent.createChooser(send, "ارسال سفارش"))
                repo.logActivity("PURCHASE_ORDER", "${list.size} items")
            }
        })
        setContentView(root)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "سفارش خرید"
    }

    override fun onSupportNavigateUp(): Boolean {
        finish(); return true
    }
}

package com.example.anbar

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView

class LicenseActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        LocaleHelper.applyLocaleToActivity(this)
        super.onCreate(savedInstanceState)
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(20, 20, 20, 28)
            setBackgroundColor(0xFFF8FAFC.toInt())
        }
        root.addView(TextView(this).apply {
            text = "لایسنس کالانو"
            textSize = 22f
            setTypeface(null, android.graphics.Typeface.BOLD)
            setTextColor(0xFF0D47A1.toInt())
        })
        val plan = LicenseManager.plan(this)
        root.addView(TextView(this).apply {
            text = "پلن فعلی: ${plan.titleFa}\nسقف کالا: ${plan.maxProducts}"
            textSize = 15f
            setPadding(0, 12, 0, 8)
        })

        val fp = LicenseManager.deviceFingerprint(this)
        root.addView(TextView(this).apply {
            text = "شناسه دستگاه (برای دریافت کد):\n$fp"
            textSize = 12f
            setTextIsSelectable(true)
            setPadding(0, 4, 0, 8)
            setTextColor(0xFF334155.toInt())
        })
        root.addView(MaterialButton(this, null, com.google.android.material.R.attr.materialButtonOutlinedStyle).apply {
            text = "کپی شناسه دستگاه"
            setOnClickListener {
                val cm = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                cm.setPrimaryClip(ClipData.newPlainText("device", fp))
                Toast.makeText(this@LicenseActivity, "کپی شد", Toast.LENGTH_SHORT).show()
            }
        })

        fun card(title: String, body: String) {
            val c = MaterialCardView(this).apply {
                radius = 14f; cardElevation = 0f; useCompatPadding = true
                setCardBackgroundColor(0xFFFFFFFF.toInt())
                setContentPadding(16, 14, 16, 14)
            }
            val col = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
            col.addView(TextView(this).apply {
                text = title
                setTypeface(null, android.graphics.Typeface.BOLD)
            })
            col.addView(TextView(this).apply {
                text = body
                textSize = 13f
                setTextColor(0xFF475569.toInt())
                setPadding(0, 6, 0, 0)
            })
            c.addView(col)
            root.addView(c)
        }
        card("رایگان", "صندوق، انبار پایه، تا ۸۰ کالا")
        card("حرفه‌ای", "گزارش، سفارش خرید، حسابرسی، بکاپ — کد وابسته به دستگاه یا کد اصلی پشتیبانی")
        card("سازمانی", "چند کاربر + همه قابلیت‌ها")

        val et = EditText(this).apply {
            hint = "کد فعال‌سازی"
            setPadding(28, 24, 28, 24)
        }
        root.addView(et)
        root.addView(MaterialButton(this).apply {
            text = "فعال‌سازی"
            setOnClickListener {
                val ok = LicenseManager.activate(this@LicenseActivity, et.text?.toString().orEmpty())
                if (ok) {
                    Toast.makeText(
                        this@LicenseActivity,
                        "فعال شد: ${LicenseManager.plan(this@LicenseActivity).titleFa}",
                        Toast.LENGTH_LONG
                    ).show()
                    recreate()
                } else {
                    Toast.makeText(this@LicenseActivity, "کد نامعتبر یا مربوط به دستگاه دیگر است", Toast.LENGTH_LONG).show()
                }
            }
        })
        root.addView(MaterialButton(this, null, com.google.android.material.R.attr.materialButtonOutlinedStyle).apply {
            text = "بازگشت به رایگان"
            setOnClickListener {
                LicenseManager.clear(this@LicenseActivity)
                Toast.makeText(this@LicenseActivity, "پلن رایگان", Toast.LENGTH_SHORT).show()
                recreate()
            }
        })

        // فقط در debug برای توسعه‌دهنده — در release با minify کمتر در دسترس
        root.addView(TextView(this).apply {
            text = "پشتیبانی: mahditariri@gmail.com\nشناسه را بفرستید تا کد دستگاه‌محور برایتان صادر شود."
            textSize = 12f
            setPadding(0, 16, 0, 0)
            setTextColor(0xFF64748B.toInt())
        })

        setContentView(ScrollView(this).apply { addView(root) })
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "لایسنس"
    }

    override fun onSupportNavigateUp(): Boolean {
        finish(); return true
    }
}

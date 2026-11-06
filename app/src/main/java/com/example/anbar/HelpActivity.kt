package com.example.anbar

import android.os.Bundle
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.card.MaterialCardView

/** راهنمای کاربردی داخل برنامه */
class HelpActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        LocaleHelper.applyLocaleToActivity(this)
        super.onCreate(savedInstanceState)
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(16, 16, 16, 28)
            setBackgroundColor(0xFFF8FAFC.toInt())
        }
        root.addView(TextView(this).apply {
            text = "راهنمای کالانو"
            textSize = 22f
            setTypeface(null, android.graphics.Typeface.BOLD)
            setTextColor(0xFF0D47A1.toInt())
        })

        fun qa(q: String, a: String) {
            val c = MaterialCardView(this).apply {
                radius = 14f; cardElevation = 0f; useCompatPadding = true
                setCardBackgroundColor(0xFFFFFFFF.toInt())
                setContentPadding(16, 14, 16, 14)
            }
            val col = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
            col.addView(TextView(this).apply {
                text = q
                setTypeface(null, android.graphics.Typeface.BOLD)
                textSize = 14f
                setTextColor(0xFF1565C0.toInt())
            })
            col.addView(TextView(this).apply {
                text = a
                textSize = 13f
                setTextColor(0xFF334155.toInt())
                setPadding(0, 6, 0, 0)
                setLineSpacing(4f, 1.1f)
            })
            c.addView(col)
            root.addView(c)
        }

        qa("چطور بفروشم؟", "حالت «فروشگاه» را انتخاب کنید → صندوق فروش → کالا را جستجو یا اسکن کنید → ثبت.")
        qa("چطور کالا وارد انبار کنم؟", "حالت «انبار» → کالای جدید (اگر نیست) → ورود به انبار.")
        qa("فاکتور را کجا ببینم؟", "فروشگاه → فاکتورها. از همان‌جا می‌توانید برگشت بزنید.")
        qa("نسیه و بدهی مشتری؟", "در صندوق، فروش نسیه را بزنید. بدهکاران را از منوی «بدهکاران» پیگیری کنید.")
        qa("بارکد کار نمی‌کند؟", "مجوز دوربین را بدهید. برای کالای بدون بارکد، روی فیلد بارکد لمس طولانی کنید تا بارکد داخلی ساخته شود.")
        qa("پشتیبان‌گیری؟", "انبار یا تنظیمات → پشتیبان‌گیری. منظم بکاپ بگیرید.")
        qa("تاریخ شمسی؟", "تنظیمات → نوع تقویم → شمسی یا میلادی.")
        qa("زبان برنامه؟", "تنظیمات → فارسی / English / العربية.")
        qa("پرفروش‌ها در صندوق؟", "زیر جعبه جستجو، کالاهای پرفروش را یک لمس کنید. «سبد قبلی» آخرین فروش را برمی‌گرداند.")
        qa("برگشت جزئی؟", "فاکتورها → برگشت → «برگشت بعضی اقلام» را انتخاب کنید.")
        qa("حداقل موجودی؟", "هنگام ثبت کالا، حداقل موجودی را بگذارید تا کمبود هشدار داده شود.")
        qa("دیتابیس نسخه ۶؟", "کالانو ۶ داده‌ها را در Room نگه می‌دارد و از نسخه قبل خودکار منتقل می‌کند. بکاپ بگیرید.")
        qa("لایسنس؟", "منوی لایسنس: رایگان / حرفه‌ای (KALANO-PRO-2026) / سازمانی (KALANO-ENT-2026)")
        qa("سفارش خرید؟", "در پلن حرفه‌ای از روی کمبود موجودی متن سفارش می‌سازد.")
        qa("پشتیبانی", "mahditariri@gmail.com — مهدی طریری")

        setContentView(ScrollView(this).apply { addView(root) })
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "راهنما"
    }

    override fun onSupportNavigateUp(): Boolean { finish(); return true }
}

package com.example.anbar

import android.content.Intent
import android.os.Bundle
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import java.io.File

class AuditLogActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        LocaleHelper.applyLocaleToActivity(this)
        super.onCreate(savedInstanceState)
        if (!LicenseManager.canUseAuditExport(this)) {
            Toast.makeText(this, LicenseManager.featureBlockedMessage("حرفه‌ای"), Toast.LENGTH_LONG).show()
            startActivity(Intent(this, LicenseActivity::class.java))
            finish()
            return
        }
        val repo = InventoryRepository(this)
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(14, 14, 14, 20)
            setBackgroundColor(0xFFF1F5F9.toInt())
        }
        root.addView(TextView(this).apply {
            text = "حسابرسی تغییرات"
            textSize = 20f
            setTypeface(null, android.graphics.Typeface.BOLD)
        })
        val logs = repo.getActivityLogs(200)
        val box = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        if (logs.isEmpty()) {
            box.addView(TextView(this).apply { text = "هنوز لاگی نیست"; setPadding(8, 20, 8, 8) })
        } else {
            logs.forEach { log ->
                box.addView(TextView(this).apply {
                    text = "${FormatUtils.dateTime(log.timestamp)}\n${log.action}: ${log.detail}"
                    textSize = 12f
                    setPadding(10, 10, 10, 10)
                    setBackgroundColor(0xFFFFFFFF.toInt())
                })
                box.addView(TextView(this).apply { text = " "; textSize = 4f })
            }
        }
        root.addView(ScrollView(this).apply {
            layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 0, 1f)
            addView(box)
        })
        root.addView(MaterialButton(this).apply {
            text = "خروجی متنی"
            setOnClickListener {
                val text = logs.joinToString("\n") {
                    "${FormatUtils.dateTime(it.timestamp)}\t${it.action}\t${it.detail}"
                }
                val f = File(cacheDir, "audit_${System.currentTimeMillis()}.txt")
                f.writeText(text)
                val uri = androidx.core.content.FileProvider.getUriForFile(
                    this@AuditLogActivity, "$packageName.fileprovider", f
                )
                startActivity(Intent.createChooser(Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_STREAM, uri)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }, "اشتراک لاگ"))
            }
        })
        setContentView(root)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "حسابرسی"
    }

    override fun onSupportNavigateUp(): Boolean {
        finish(); return true
    }
}

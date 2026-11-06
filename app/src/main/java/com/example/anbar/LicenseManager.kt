package com.example.anbar

import android.content.Context
import android.provider.Settings
import java.security.MessageDigest
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

/**
 * لایسنس سخت‌تر:
 * - کد عمومی ثابت داخل APK به‌صورت تکه‌تکه + مشتق (نه یک رشته کامل واضح)
 * - کد وابسته به دستگاه: KLN1-PRO-XXXXXXXX / KLN1-ENT-XXXXXXXX
 * - ذخیره پلن با امضای دستگاه (اگر دستگاه عوض شود، پلن باطل)
 *
 * تولید کد برای مشتری: در صفحه لایسنس «شناسه دستگاه» را بفرستد؛
 * یا از تابع expectedDeviceCode برای پشتیبانی.
 */
object LicenseManager {

    enum class Plan(val titleFa: String, val maxProducts: Int) {
        FREE("رایگان", 80),
        PRO("حرفه‌ای", 5_000),
        ENTERPRISE("سازمانی", 50_000)
    }

    private const val PREFS = "kalano_license_v2"
    private const val KEY_PLAN = "plan"
    private const val KEY_CODE = "code"
    private const val KEY_SIG = "sig"
    private const val KEY_ACTIVATED_AT = "activated_at"

    /** قطعات کلید — یکجا به‌صورت رشته کامل در اسمبلی دیده نمی‌شود */
    private fun secretMaterial(): ByteArray {
        val a = byteArrayOf(0x4B, 0x61, 0x6C, 0x61, 0x6E, 0x6F) // Kalano
        val b = byteArrayOf(0x54, 0x61, 0x72, 0x69, 0x72, 0x69) // Tariri
        val c = byteArrayOf(0x32, 0x30, 0x32, 0x36, 0x21, 0x53) // 2026!S
        return a + b + c + byteArrayOf(0x9C.toByte(), 0x3A, 0x71, 0xE2.toByte())
    }

    fun deviceFingerprint(context: Context): String {
        val androidId = try {
            Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID) ?: "unknown"
        } catch (_: Exception) {
            "unknown"
        }
        val pkg = context.packageName
        return sha256Hex("$androidId|$pkg").take(16).uppercase()
    }

    /** کد اختصاصی همین دستگاه برای پلن مشخص (برای پشتیبانی) */
    fun expectedDeviceCode(context: Context, plan: Plan): String {
        if (plan == Plan.FREE) return ""
        val tag = if (plan == Plan.PRO) "PRO" else "ENT"
        val token = hmacHex(secretMaterial(), "${deviceFingerprint(context)}|$tag").take(8).uppercase()
        return "KLN1-$tag-$token"
    }

    fun plan(context: Context): Plan {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val stored = prefs.getString(KEY_PLAN, "FREE") ?: "FREE"
        val plan = try {
            Plan.valueOf(stored)
        } catch (_: Exception) {
            Plan.FREE
        }
        if (plan == Plan.FREE) return Plan.FREE
        // یکپارچگی با دستگاه
        val sig = prefs.getString(KEY_SIG, "") ?: ""
        val expect = integritySig(context, plan)
        if (sig != expect) {
            // دستکاری یا تعویض دستگاه
            prefs.edit().putString(KEY_PLAN, Plan.FREE.name).apply()
            return Plan.FREE
        }
        return plan
    }

    fun activationCode(context: Context): String =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getString(KEY_CODE, "") ?: ""

    fun activatedAt(context: Context): Long =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getLong(KEY_ACTIVATED_AT, 0L)

    fun activate(context: Context, rawCode: String): Boolean {
        val code = rawCode.trim().uppercase().replace(" ", "").replace("_", "-")
        if (code.isBlank()) return false

        val plan = resolvePlan(context, code) ?: return false

        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit()
            .putString(KEY_PLAN, plan.name)
            .putString(KEY_CODE, code)
            .putString(KEY_SIG, integritySig(context, plan))
            .putLong(KEY_ACTIVATED_AT, System.currentTimeMillis())
            .apply()
        return true
    }

    private fun resolvePlan(context: Context, code: String): Plan? {
        // ۱) کد وابسته به همین دستگاه
        if (code == expectedDeviceCode(context, Plan.PRO)) return Plan.PRO
        if (code == expectedDeviceCode(context, Plan.ENTERPRISE)) return Plan.ENTERPRISE

        // ۲) کد عمومی مشتق‌شده (نه رشتهٔ ثابت ساده در when)
        if (matchesMaster(code, Plan.PRO)) return Plan.PRO
        if (matchesMaster(code, Plan.ENTERPRISE)) return Plan.ENTERPRISE

        // ۳) قالب KLN1-PRO-xxxxxxxx برای همین دستگاه
        val parts = code.split("-")
        if (parts.size == 3 && parts[0] == "KLN1") {
            val tag = parts[1]
            val token = parts[2]
            val plan = when (tag) {
                "PRO" -> Plan.PRO
                "ENT", "ENTERPRISE" -> Plan.ENTERPRISE
                else -> return null
            }
            val expect = hmacHex(secretMaterial(), "${deviceFingerprint(context)}|$tag").take(8).uppercase()
            if (token == expect) return plan
        }
        return null
    }

    private fun matchesMaster(code: String, plan: Plan): Boolean {
        // master = HMAC("MASTER|" + plan) اول ۸ + پیشوند
        val tag = if (plan == Plan.PRO) "PRO" else "ENT"
        val token = hmacHex(secretMaterial(), "MASTER|$tag|KALANO").take(10).uppercase()
        val expected = "KLN0-$tag-$token"
        return code == expected
    }

    /** برای پشتیبانی: کد عمومی (غیر وابسته به دستگاه) */
    fun masterCode(plan: Plan): String {
        val tag = if (plan == Plan.PRO) "PRO" else "ENT"
        val token = hmacHex(secretMaterial(), "MASTER|$tag|KALANO").take(10).uppercase()
        return "KLN0-$tag-$token"
    }

    private fun integritySig(context: Context, plan: Plan): String =
        hmacHex(secretMaterial(), "SIG|${plan.name}|${deviceFingerprint(context)}").take(24)

    fun clear(context: Context) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().clear().apply()
        // مهاجرت از prefs قدیمی
        context.getSharedPreferences("kalano_license", Context.MODE_PRIVATE).edit().clear().apply()
    }

    fun canAddProduct(context: Context, currentCount: Int): Boolean =
        currentCount < plan(context).maxProducts

    fun canUseAdvancedReports(context: Context): Boolean =
        plan(context) != Plan.FREE

    fun canUseMultiUser(context: Context): Boolean =
        plan(context) == Plan.ENTERPRISE

    fun canUsePurchaseOrder(context: Context): Boolean =
        plan(context) != Plan.FREE

    fun canUseAuditExport(context: Context): Boolean =
        plan(context) != Plan.FREE

    fun canUseCloudBackupUi(context: Context): Boolean =
        plan(context) != Plan.FREE

    fun featureBlockedMessage(planNeeded: String = "حرفه‌ای"): String =
        "این قابلیت در نسخه $planNeeded فعال است.\nاز منوی لایسنس کد فعال‌سازی را وارد کنید."

    private fun sha256Hex(s: String): String {
        val d = MessageDigest.getInstance("SHA-256").digest(s.toByteArray(Charsets.UTF_8))
        return d.joinToString("") { "%02x".format(it) }
    }

    private fun hmacHex(key: ByteArray, data: String): String {
        val mac = Mac.getInstance("HmacSHA256")
        mac.init(SecretKeySpec(key, "HmacSHA256"))
        return mac.doFinal(data.toByteArray(Charsets.UTF_8)).joinToString("") { "%02x".format(it) }
    }
}

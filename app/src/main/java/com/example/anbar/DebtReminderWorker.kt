package com.example.anbar

import android.content.Context
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.Worker
import androidx.work.WorkerParameters
import java.util.concurrent.TimeUnit

/** یادآوری روزانه بدهی‌ها (نوتیفیکیشن) */
class DebtReminderWorker(ctx: Context, params: WorkerParameters) : Worker(ctx, params) {
    override fun doWork(): Result {
        return try {
            val repo = InventoryRepository(applicationContext)
            val debtors = repo.getCustomersWithDebt()
            if (debtors.isNotEmpty()) {
                val total = debtors.sumOf { it.creditBalance }
                NotificationHelper.show(
                    applicationContext,
                    "kalano_stock",
                    42,
                    "یادآوری وصول",
                    "${debtors.size} بدهکار · جمع ${FormatUtils.money(total)}"
                )
            }
            Result.success()
        } catch (_: Exception) {
            Result.retry()
        }
    }

    companion object {
        fun schedule(context: Context) {
            val req = PeriodicWorkRequestBuilder<DebtReminderWorker>(24, TimeUnit.HOURS).build()
            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                "kalano_debt_reminder",
                ExistingPeriodicWorkPolicy.KEEP,
                req
            )
        }
    }
}

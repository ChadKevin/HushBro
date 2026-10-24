package com.invictus.hushbro.util

import android.content.Context
import com.invictus.hushbro.data.repo.NotificationRepository
import kotlinx.coroutines.flow.first
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CsvExporter @Inject constructor(
    @dagger.hilt.android.qualifiers.ApplicationContext private val context: Context,
    private val notificationRepository: NotificationRepository
) {
    suspend fun export(): File {
        val notifications = notificationRepository.getAllNotifications().first()
        val file = File(context.cacheDir, "hushbro_export.csv")
        file.bufferedWriter().use { w ->
            w.write("id,appLabel,packageName,title,text,tag,status,postTime,isRead\n")
            notifications.forEach { n ->
                val row = listOf(
                    n.id.toString(),
                    n.appLabel,
                    n.packageName,
                    n.title ?: "",
                    n.text ?: "",
                    n.tag.name,
                    n.status.name,
                    n.postTime.toString(),
                    n.isRead.toString()
                ).joinToString(",") { "\"${it.replace("\"", "\"\"")}\"" }
                w.write(row + "\n")
            }
        }
        return file
    }
}

package com.javis.launcher.services

import android.content.pm.PackageManager
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import com.javis.launcher.JavisApplication
import com.javis.launcher.database.entities.NotificationCacheEntity
import com.javis.launcher.models.NotificationItem
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class JavisNotificationListener : NotificationListenerService() {

    private val scope = CoroutineScope(Dispatchers.IO)

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        sbn ?: return
        val extras = sbn.notification.extras
        val title = extras.getString("android.title") ?: return
        val text = extras.getString("android.text") ?: ""
        val appName = try { packageManager.getApplicationLabel(
            packageManager.getApplicationInfo(sbn.packageName, 0)).toString()
        } catch (e: Exception) { sbn.packageName }
        val item = NotificationItem(sbn.packageName, appName, title, text, sbn.postTime)
        notifications[sbn.packageName] = (notifications[sbn.packageName] ?: mutableListOf()).also { it.add(item) }
        scope.launch {
            try {
                JavisApplication.instance.database.notificationCacheDao().insert(
                    NotificationCacheEntity(packageName = sbn.packageName, appName = appName, title = title, content = text, timestamp = sbn.postTime)
                )
            } catch (_: Exception) {}
        }
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification?) {
        sbn ?: return
    }

    companion object {
        private val notifications = mutableMapOf<String, MutableList<NotificationItem>>()

        fun getAllNotifications(): List<NotificationItem> = notifications.values.flatten().sortedByDescending { it.timestamp }.take(50)

        fun getSummary(): String {
            val all = getAllNotifications()
            if (all.isEmpty()) return "No new notifications, Sir."
            val grouped = all.groupBy { it.appName }
            return "You have ${all.size} notifications from ${grouped.size} apps: " +
                grouped.entries.take(5).joinToString(", ") { "${it.value.size} from ${it.key}" } + "."
        }

        fun clearAll() = notifications.clear()
    }
}

package com.javis.launcher.services

import android.content.pm.PackageManager
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import com.javis.launcher.JavisApplication
import com.javis.launcher.database.entities.NotificationHistoryEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class JavisNotificationListener : NotificationListenerService() {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val _notificationMap = MutableStateFlow<Map<String, List<StatusBarNotification>>>(emptyMap())

    companion object {
        val notificationMap: MutableStateFlow<Map<String, MutableList<String>>> = MutableStateFlow(emptyMap())

        fun getSummary(): String {
            val map = notificationMap.value
            if (map.isEmpty()) return "No new notifications"
            val sb = StringBuilder("You have:\n")
            map.forEach { (app, msgs) ->
                sb.append("• ${msgs.size} ${if (msgs.size == 1) "notification" else "notifications"} from $app\n")
            }
            return sb.toString().trim()
        }

        fun clearAll() { notificationMap.value = emptyMap() }
    }

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        val pkg = sbn.packageName
        val pm = applicationContext.packageManager
        val appName = try { pm.getApplicationLabel(pm.getApplicationInfo(pkg, 0)).toString() }
                      catch (e: Exception) { pkg }
        val text = sbn.notification.extras.getCharSequence("android.text")?.toString() ?: ""
        val title = sbn.notification.extras.getCharSequence("android.title")?.toString() ?: ""
        val content = if (text.isNotBlank()) "$title: $text" else title

        val current = notificationMap.value.toMutableMap()
        val list = current.getOrDefault(appName, mutableListOf())
        (list as? MutableList)?.add(content) ?: run { current[appName] = mutableListOf(content) }
        current[appName] = (current[appName] as? MutableList ?: mutableListOf(content))
        notificationMap.value = current

        scope.launch {
            try {
                val app = JavisApplication.instance
                app.database.notificationHistoryDao().insert(
                    NotificationHistoryEntity(packageName = pkg, appName = appName, title = title, text = text)
                )
            } catch (_: Exception) {}
        }
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification) {
        val pm = applicationContext.packageManager
        val appName = try { pm.getApplicationLabel(pm.getApplicationInfo(sbn.packageName, 0)).toString() }
                      catch (e: Exception) { sbn.packageName }
        val current = notificationMap.value.toMutableMap()
        current.remove(appName)
        notificationMap.value = current
    }
}

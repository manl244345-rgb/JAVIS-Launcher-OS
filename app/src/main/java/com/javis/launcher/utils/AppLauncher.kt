package com.javis.launcher.utils

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.ContactsContract
import android.telecom.TelecomManager
import com.javis.launcher.JavisApplication
import com.javis.launcher.models.AppInfo
import com.javis.launcher.models.ContactInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object AppLauncher {

    suspend fun findAndLaunchApp(context: Context, appName: String): LaunchResult = withContext(Dispatchers.IO) {
        val pm = context.packageManager
        val searchName = appName.lowercase().trim()
        val allApps = pm.getInstalledPackages(0)
        val found = allApps.firstOrNull { pkg ->
            val label = pm.getApplicationLabel(pkg.applicationInfo).toString().lowercase()
            label.contains(searchName) || searchName.contains(label.split(" ").first())
        }
        if (found != null) {
            val intent = pm.getLaunchIntentForPackage(found.packageName)
            if (intent != null) {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                withContext(Dispatchers.Main) { context.startActivity(intent) }
                val db = JavisApplication.instance.database
                db.installedAppDao().incrementLaunchCount(found.packageName)
                LaunchResult.Success(pm.getApplicationLabel(found.applicationInfo).toString())
            } else {
                LaunchResult.NotLaunchable(appName)
            }
        } else {
            LaunchResult.NotFound(appName)
        }
    }

    suspend fun getInstalledApps(context: Context): List<AppInfo> = withContext(Dispatchers.IO) {
        val pm = context.packageManager
        val mainIntent = Intent(Intent.ACTION_MAIN, null).apply { addCategory(Intent.CATEGORY_LAUNCHER) }
        pm.queryIntentActivities(mainIntent, 0)
            .filter { it.activityInfo.packageName != context.packageName }
            .map { ri ->
                AppInfo(
                    packageName = ri.activityInfo.packageName,
                    appName = ri.loadLabel(pm).toString(),
                    isSystemApp = (ri.activityInfo.applicationInfo.flags and android.content.pm.ApplicationInfo.FLAG_SYSTEM) != 0
                )
            }
            .sortedBy { it.appName }
    }

    suspend fun searchContacts(context: Context, query: String): List<ContactInfo> = withContext(Dispatchers.IO) {
        val contacts = mutableListOf<ContactInfo>()
        val cursor = context.contentResolver.query(
            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
            arrayOf(
                ContactsContract.CommonDataKinds.Phone.CONTACT_ID,
                ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                ContactsContract.CommonDataKinds.Phone.NUMBER
            ),
            "${ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME} LIKE ?",
            arrayOf("%$query%"), null
        )
        cursor?.use {
            while (it.moveToNext()) {
                contacts.add(ContactInfo(
                    id = it.getString(0) ?: "",
                    name = it.getString(1) ?: "",
                    phone = it.getString(2) ?: ""
                ))
            }
        }
        contacts.distinctBy { it.id }
    }

    fun callContact(context: Context, phone: String): Boolean {
        return try {
            val intent = Intent(Intent.ACTION_CALL, Uri.parse("tel:$phone"))
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
            true
        } catch (e: Exception) { false }
    }
}

sealed class LaunchResult {
    data class Success(val appName: String) : LaunchResult()
    data class NotFound(val query: String) : LaunchResult()
    data class NotLaunchable(val appName: String) : LaunchResult()
}

package com.javis.launcher.utils

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.ContactsContract
import com.javis.launcher.models.AppInfo
import com.javis.launcher.models.ContactInfo
import com.javis.launcher.models.LaunchResult

object AppLauncher {

    fun getAllApps(context: Context): List<AppInfo> {
        val pm = context.packageManager
        val intent = Intent(Intent.ACTION_MAIN, null).apply { addCategory(Intent.CATEGORY_LAUNCHER) }
        return pm.queryIntentActivities(intent, 0)
            .filter { it.activityInfo.packageName != context.packageName }
            .map { ri ->
                AppInfo(
                    packageName = ri.activityInfo.packageName,
                    appName = ri.loadLabel(pm).toString(),
                    icon = ri.loadIcon(pm)
                )
            }
            .sortedBy { it.appName }
    }

    fun findAndLaunchApp(context: Context, query: String): LaunchResult {
        val apps = getAllApps(context)
        val lower = query.lowercase().trim()
        val exact = apps.find { it.appName.lowercase() == lower }
        val starts = apps.find { it.appName.lowercase().startsWith(lower) }
        val contains = apps.find { it.appName.lowercase().contains(lower) }
        val match = exact ?: starts ?: contains ?: return LaunchResult.NotFound(query)
        return try {
            val intent = context.packageManager.getLaunchIntentForPackage(match.packageName)
                ?: return LaunchResult.NotLaunchable(match.appName)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
            LaunchResult.Success(match.appName)
        } catch (e: Exception) { LaunchResult.NotLaunchable(match.appName) }
    }

    fun searchContacts(context: Context, query: String): List<ContactInfo> {
        val results = mutableListOf<ContactInfo>()
        val lower = query.lowercase()
        val cursor = context.contentResolver.query(
            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
            arrayOf(
                ContactsContract.CommonDataKinds.Phone.CONTACT_ID,
                ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                ContactsContract.CommonDataKinds.Phone.NUMBER,
                ContactsContract.CommonDataKinds.Phone.PHOTO_URI
            ),
            "${ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME} LIKE ?",
            arrayOf("%$query%"), null
        ) ?: return results
        cursor.use {
            while (it.moveToNext()) {
                val id = it.getString(0) ?: continue
                val name = it.getString(1) ?: continue
                val phone = it.getString(2) ?: continue
                val photo = it.getString(3)
                if (results.none { r -> r.id == id })
                    results.add(ContactInfo(id, name, phone, photo))
            }
        }
        return results.filter { it.name.lowercase().contains(lower) }
    }

    fun callContact(context: Context, phone: String) {
        val intent = Intent(Intent.ACTION_CALL, Uri.parse("tel:${phone.replace(" ", "")}"))
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    }

    fun openSettings(context: Context) {
        val i = Intent(android.provider.Settings.ACTION_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(i)
    }
}

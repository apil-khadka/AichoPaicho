package dev.nyxigale.aichopaicho.ui.util

import android.content.ContentUris
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.ContactsContract
import dev.nyxigale.aichopaicho.data.entity.Contact

object IntentUtils {
    fun openContactDetails(context: Context, contact: Contact?): Boolean {
        val contactId = contact?.contactId?.toLongOrNull() ?: return false
        val contactUri = ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, contactId)
        val viewIntent = Intent(Intent.ACTION_VIEW, contactUri)
        // Note: resolveActivity might return null on Android 11+ without <queries> in Manifest.
        // We already added <queries> to Manifest.
        return try {
            context.startActivity(viewIntent)
            true
        } catch (_: Exception) {
            false
        }
    }

    fun openDialer(context: Context, phoneNumber: String?): Boolean {
        val sanitizedNumber = phoneNumber?.trim()?.takeIf { it.isNotEmpty() } ?: return false
        val dialIntent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${Uri.encode(sanitizedNumber)}"))
        return try {
            context.startActivity(dialIntent)
            true
        } catch (_: Exception) {
            false
        }
    }
}

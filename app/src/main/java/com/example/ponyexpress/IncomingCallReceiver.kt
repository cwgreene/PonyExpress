package com.example.ponyexpress

import android.content.BroadcastReceiver
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.media.AudioManager
import android.net.Uri
import android.provider.ContactsContract
import android.telephony.TelephonyManager
import android.util.Log


class IncomingCallReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {

        if (intent.getStringExtra(TelephonyManager.EXTRA_STATE) == TelephonyManager.EXTRA_STATE_RINGING) {
            val incomingNumber = intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER)
            Log.d("IncomingCallReceiver", "Incoming number: $incomingNumber")

            if (isSpecialContact(context, incomingNumber)) {
                setMaxVolume(context)
            }
        }
    }

    fun getSchema(contentResolver: ContentResolver, contentUri: Uri) {
        // Query the content provider for metadata
        val cursor = contentResolver.query(
            contentUri,
            null,
            null,
            null,
            null
        )
        if (cursor != null) {
            cursor.moveToFirst()
            Log.d("doom", " "+ cursor.position)
            // Get column names and types
            val columnNames = cursor.columnNames
            for (columnName in columnNames) {
                val columnIndex = cursor.getColumnIndex(columnName)
                if (columnIndex < 0) {
                    continue
                }
                Log.d("doom", " " + columnName)
                /*val columnType = cursor.getType(columnIndex)
                var columnTypeString: String
                columnTypeString = when (columnType) {
                    Cursor.FIELD_TYPE_INTEGER -> "INTEGER"
                    Cursor.FIELD_TYPE_FLOAT -> "FLOAT"
                    Cursor.FIELD_TYPE_STRING -> "STRING"
                    Cursor.FIELD_TYPE_BLOB -> "BLOB"
                    Cursor.FIELD_TYPE_NULL -> "NULL"
                    else -> "UNKNOWN"
                }*/

                // Print column name and type
                //Log.d("doom", "Column: $columnName, Type: $columnTypeString")
            }
            cursor.close()
        } else {
            Log.d("doom", "failed to get content")
        }
    }


    private fun isSpecialContact(context: Context, incomingNumber: String?): Boolean {
        val uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(incomingNumber))
        val cursor: Cursor? = context.contentResolver.query(uri, arrayOf(ContactsContract.PhoneLookup.DISPLAY_NAME), "display_name=?",
            arrayOf( incomingNumber), null)
        getSchema(context.contentResolver, uri)

        cursor?.use {
            if (it.moveToFirst()) {
                val nameColumnIdx = it.getColumnIndex(ContactsContract.PhoneLookup.DISPLAY_NAME)
                if (nameColumnIdx == -1) {
                    return false;
                }
                val contactName = it.getString(nameColumnIdx)
                Log.d("IncomingCallReceiver", "Contact name: $contactName")
                return contactName == "Bob Matthews" // Replace with your special contact's name
            }
        }
        return false
    }

    private fun setMaxVolume(context: Context) {
        val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        val maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_RING)
        audioManager.setStreamVolume(AudioManager.STREAM_RING, maxVolume, AudioManager.FLAG_SHOW_UI)
        Log.d("Hello", "Raised volume")
    }
}

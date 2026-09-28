package com.example.smsforwarder

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.telephony.SmsMessage
import kotlinx.coroutines.*
import java.net.HttpURLConnection
import java.net.URL
import java.text.SimpleDateFormat
import java.util.*

class SMSReceiver : BroadcastReceiver() {
    
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == "android.provider.Telephony.SMS_RECEIVED") {
            val bundle = intent.extras
            val pdus = bundle?.get("pdus") as? Array<*>
            
            pdus?.forEach { pdu ->
                val sms = SmsMessage.createFromPdu(pdu as ByteArray)
                val sender = sms.originatingAddress ?: "Unknown"
                val message = sms.messageBody ?: ""
                val timestamp = System.currentTimeMillis()
                
                // Forward to server
                forwardSMS(context, sender, message, timestamp)
            }
        }
    }

    private fun forwardSMS(context: Context, sender: String, message: String, timestamp: Long) {
        val prefs = context.getSharedPreferences("SMSForwarder", Context.MODE_PRIVATE)
        val serverUrl = prefs.getString("server_url", "http://192.168.1.100:3000") 
            ?: "http://192.168.1.100:3000"

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val url = URL("$serverUrl/api/sms")
                val conn = url.openConnection() as HttpURLConnection
                conn.requestMethod = "POST"
                conn.setRequestProperty("Content-Type", "application/json")
                conn.doOutput = true

                val json = """
                    {
                        "sender": "${sender.replace("\"", "\\\"")}",
                        "message": "${message.replace("\"", "\\\"").replace("\n", "\\n")}",
                        "timestamp": $timestamp,
                        "device": "${android.os.Build.MODEL}"
                    }
                """.trimIndent()

                conn.outputStream.write(json.toByteArray())
                conn.outputStream.close()
                
                val response = conn.responseCode
                android.util.Log.d("SMSForwarder", "Server response: $response")
                conn.disconnect()
            } catch (e: Exception) {
                android.util.Log.e("SMSForwarder", "Failed to forward: ${e.message}")
            }
        }
    }
}
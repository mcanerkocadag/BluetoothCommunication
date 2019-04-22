package com.example.bluetoothcommunication

import android.bluetooth.BluetoothSocket
import android.os.Handler
import java.io.InputStream
import java.io.OutputStream
import java.lang.Exception

class SendReceiver(activityBluetoothSocket: BluetoothSocket, activityHandler: Handler) : Thread() {

    var bluetoothSocket: BluetoothSocket = activityBluetoothSocket
    var inputStream: InputStream? = null
    var outputStream: OutputStream? = null
    lateinit var handler: Handler

    init {
        inputStream = bluetoothSocket.inputStream
        outputStream = bluetoothSocket.outputStream
        handler = activityHandler
    }

    override fun run() {

        val myBuffer: ByteArray = ByteArray(1024)
        var bytes: Int
        while (true) {

            try {

                bytes = inputStream!!.read(myBuffer)
                handler.obtainMessage(Sabitler.STATE_MESSAGE_RECEİVED, bytes, -1,myBuffer).sendToTarget()
            } catch (ex: Exception) {

            }

        }
    }


}
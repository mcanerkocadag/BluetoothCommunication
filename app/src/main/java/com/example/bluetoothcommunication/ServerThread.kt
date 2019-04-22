package com.example.bluetoothcommunication

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothServerSocket
import android.bluetooth.BluetoothSocket
import android.os.Handler
import android.os.Message
import java.lang.Exception

class ServerThread(bluetoothAdapter: BluetoothAdapter, activityHandler: Handler) : Thread() {

    var bluetoothAdapter = bluetoothAdapter
    lateinit var handler: Handler
    lateinit var serverSocket: BluetoothServerSocket

    init {
        serverSocket = bluetoothAdapter.listenUsingRfcommWithServiceRecord(Sabitler.APP_NAME, Sabitler.Uuıd)
        handler = activityHandler

    }

    override fun run() {

        var socket: BluetoothSocket? = null
        while (socket == null){

            try {

                var msg :Message = Message.obtain()
                msg.what = Sabitler.STATE_CONNECTING
                handler.sendMessage(msg)
                socket = serverSocket.accept()
            }catch (ex:Exception){

                var msg :Message = Message.obtain()
                msg.what = Sabitler.STATE_CONNECTION_FAILED
                handler.sendMessage(msg)
            }

            if(socket != null){

                var msg :Message = Message.obtain()
                msg.what = Sabitler.STATE_CONNECTED
                handler.sendMessage(msg)

                //SendReceiver(socket,handler).start()
                var receiver = SendReceiver(socket,handler)
                receiver.start()
                Sabitler.globalReceiver = receiver
                // write some code ...
            }
        }
    }
}
package com.example.bluetoothcommunication

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.os.Handler
import android.os.Message
import java.lang.Exception

class ClientThread(bluetoothAdapter: BluetoothAdapter, activityHandler: Handler, activityDevice: BluetoothDevice) : Thread() {

    var bluetoothAdapter = bluetoothAdapter
    lateinit var handler: Handler
    lateinit var socket: BluetoothSocket
    lateinit var device: BluetoothDevice

    init {

        try {

            device = activityDevice
            socket = device.createRfcommSocketToServiceRecord(Sabitler.Uuıd)
            handler = activityHandler

        } catch (ex: Exception) {

        }

    }

    override fun run() {

        bluetoothAdapter.cancelDiscovery()

        try {

            socket.connect()
            var msg: Message = Message.obtain()
            msg.what = Sabitler.STATE_CONNECTED
            handler.sendMessage(msg)

            var receiver = SendReceiver(socket,handler)
                receiver.start()
            Sabitler.globalReceiver = receiver

        } catch (ex: Exception) {

            var msg: Message = Message.obtain()
            msg.what = Sabitler.STATE_CONNECTION_FAILED
            handler.sendMessage(msg)
        }

    }

}
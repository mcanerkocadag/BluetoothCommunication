package com.example.bluetoothcommunication

import java.util.*

class Sabitler {
    var globalReceiver =null
    companion object {

        var isRunning:Boolean = false
        var globalReceiver: SendReceiver? = null
        var APP_NAME: String = "KİMSİN"
        var Uuıd: UUID = UUID.fromString("dccc301b-579c-45a2-aed5-d5b4ed56fdc1")

        internal var STATE_LISTENING = 1
        internal var STATE_CONNECTING = 2
        internal var STATE_CONNECTED = 3
        internal var STATE_CONNECTION_FAILED = 4
        internal var STATE_MESSAGE_RECEİVED = 5

    }
}
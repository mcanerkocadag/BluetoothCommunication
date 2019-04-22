package com.example.bluetoothcommunication

import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.widget.*
import java.util.*
import kotlin.collections.ArrayList

class BluetoothActivity : AppCompatActivity() {

    private lateinit var scanButton: Button
    private lateinit var listenButton: Button
    private lateinit var clientButton: Button
    private lateinit var sendButton: Button
    private lateinit var device_list_view: ListView
    var nameList = ArrayList<String>()
    var deviceList = ArrayList<BluetoothDevice>()
    lateinit var arrayAdapter: ArrayAdapter<String>
    lateinit var selectDevice: BluetoothDevice

    companion object {

        private var REQUEST_ENABLE_BT: Int = 1
    }

    private val bluetoothReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            var actionValue = intent?.action
            if (actionValue == BluetoothDevice.ACTION_FOUND) {
                var device: BluetoothDevice? = intent?.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
                if (device?.name == null)
                    return
                nameList.add(device.name)
                deviceList.add(device)
                arrayAdapter.notifyDataSetChanged()
            }
        }

    }

    override fun onPause() {
        unregisterReceiver(bluetoothReceiver)
        super.onPause()
    }

    override fun onStart() {
        super.onStart()

        var intentfilter = IntentFilter(BluetoothDevice.ACTION_FOUND)
        registerReceiver(bluetoothReceiver, intentfilter)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_bluetooth)
        device_list_view = findViewById(R.id.device_list_view)
        scanButton = findViewById(R.id.scan_button)
        listenButton = findViewById(R.id.listen_button)
        clientButton = findViewById(R.id.client_button)
        sendButton = findViewById(R.id.send_button)


        val myBluetoothAdapter: BluetoothAdapter? = BluetoothAdapter.getDefaultAdapter()
        if (myBluetoothAdapter == null) {

            return
        } else if (!myBluetoothAdapter.isEnabled) {
            var bluetoothSetting = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            startActivityForResult(bluetoothSetting, REQUEST_ENABLE_BT)
        }

        scanButton.setOnClickListener {

            //if (myBluetoothAdapter.isDiscovering)
            //   myBluetoothAdapter.cancelDiscovery()

            myBluetoothAdapter.startDiscovery()
        }

        sendButton.setOnClickListener {
            if (!Sabitler.isRunning)
                return@setOnClickListener
            write()
        }

        arrayAdapter =
            ArrayAdapter(this, android.R.layout.simple_list_item_1, nameList)
        device_list_view.adapter = arrayAdapter

        device_list_view.onItemClickListener = object : AdapterView.OnItemClickListener {
            override fun onItemClick(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                selectDevice = deviceList[position]
            }

        }

        listenButton.setOnClickListener {
            if (Sabitler.isRunning)
                return@setOnClickListener
            ServerThread(myBluetoothAdapter, handler).start()
        }

        clientButton.setOnClickListener {

            if (Sabitler.isRunning)
                return@setOnClickListener
            ClientThread(myBluetoothAdapter, handler, selectDevice).start()
        }

    }

    var handler: Handler = Handler(Handler.Callback {


        when (it.what) {

            Sabitler.STATE_LISTENING -> showToast("Listening")
            Sabitler.STATE_CONNECTING -> showToast("Connecting")
            Sabitler.STATE_CONNECTED -> {
                showToast("Connected")
                Sabitler.isRunning = true
            }
            Sabitler.STATE_CONNECTION_FAILED -> {
                showToast("Connectiong Failed")
                Sabitler.isRunning = false
            }
            Sabitler.STATE_MESSAGE_RECEİVED -> {

                var readBuff: ByteArray = it.obj as ByteArray
                var tempMsg = String(readBuff, 0, it.arg1)
                showToast(tempMsg)

            }
            else -> {

                showToast("Message is not found")
            }
        }
        return@Callback true

    })


    fun write() {

        var text = "Hello"
        Sabitler.globalReceiver?.outputStream?.write(text.toByteArray())
    }

    fun showToast(msg: String) {

        Toast.makeText(this, "" + msg, Toast.LENGTH_SHORT).show()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)


        if (requestCode == REQUEST_ENABLE_BT) {

            if (resultCode == Activity.RESULT_OK) {

                Toast.makeText(this, "Bluetooth is Enabled", Toast.LENGTH_SHORT).show()

            } else if (resultCode == Activity.RESULT_CANCELED) {

                Toast.makeText(this, "Bluetooth is Cancelled", Toast.LENGTH_SHORT).show()
            }

        } else {

            Toast.makeText(this, "Bluetooth is Disable", Toast.LENGTH_SHORT).show()
        }

    }

    fun getPairedBluetoothDevices(): MutableSet<BluetoothDevice>? {

        var myBluetoothAdapter: BluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        if (!myBluetoothAdapter.isEnabled) {

            Toast.makeText(this, "Bluetooth is Disable", Toast.LENGTH_SHORT).show()
            return null
        }
        var list = myBluetoothAdapter.bondedDevices
        Arrays.asList(list)
        return list
    }

    fun getBluetoothDeviceNameList(): Array<String> {

        var list = getPairedBluetoothDevices()
        var array = Array<String>(list?.size!!) { "" }
        var indis = 0
        if (list.size > 0) {
            for (device in list) {

                array[indis] = device.name
                indis++
            }
        }
        return array
    }
}

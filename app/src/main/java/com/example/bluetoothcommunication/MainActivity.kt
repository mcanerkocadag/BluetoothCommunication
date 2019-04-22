package com.example.bluetoothcommunication

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothProfile.STATE_CONNECTED
import android.bluetooth.BluetoothServerSocket
import android.bluetooth.BluetoothSocket
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.util.Log
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.util.*
import kotlin.collections.ArrayList
import android.content.DialogInterface
import android.view.View
import android.widget.*
import android.widget.AdapterView.OnItemClickListener


class MainActivity : AppCompatActivity() {
    var bluetoothAdapter: BluetoothAdapter? = null
    lateinit var listview: ListView
    val deviceList = ArrayList<BluetoothDevice>()
    val deviceNameList = ArrayList<String>()
    var veriAdaptoru: ArrayAdapter<String>? = null
    lateinit var serverButton : Button
    lateinit var clientButton : Button


    companion object {

        const val REQUEST_ENABLE_BT = 100
        // Defines several constants used when transmitting messages between the
        // service and the UI.
        const val MESSAGE_READ: Int = 0
        private const val TAG = "MY_APP_DEBUG_TAG"
        const val MESSAGE_WRITE: Int = 1
        const val MESSAGE_TOAST: Int = 2
        //val MY_UUID: UUID = UUID.randomUUID()
        var pairedDevice: BluetoothDevice? = null
        private val MY_UUID = UUID.fromString("8ce255c0-200a-11e0-ac64-0800200c9a66")

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        serverButton = findViewById(R.id.server)
        clientButton = findViewById(R.id.client)

        val filter = IntentFilter(BluetoothDevice.ACTION_FOUND)
        this.registerReceiver(receiver, filter)

        val pairFilter = IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED)
        this.registerReceiver(mPairReceiver, pairFilter)
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        if (bluetoothAdapter == null) {
            Toast.makeText(this, "Device doesn't support Bluetooth", Toast.LENGTH_SHORT).show()
            return
        }
        bluetoothAdapter?.startDiscovery()

        if (!bluetoothAdapter!!.isEnabled) {
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT)
            return
        }

        Log.i("MainActivity", "Paired Devices")
        val pairedDevices: Set<BluetoothDevice>? = bluetoothAdapter?.bondedDevices
        pairedDevices?.forEach { device ->
            val deviceName = device.name
            val deviceHardwareAddress = device.address // MAC address
            Log.i("MainActivity", "Device Name: " + deviceName + " deviceHardwareAdress: " + deviceHardwareAddress)
            pairedDevice = device
            unpairDevice(device)
        }

        serverButton.setOnClickListener {

            AcceptThread().run()
        }

        clientButton.setOnClickListener {
            ConnectThread(pairedDevice).run()
        }



        Log.i("MainActivity", "Paired Devices Finish")

        listview = findViewById(R.id.listView1)

         veriAdaptoru = ArrayAdapter<String>(
            this@MainActivity,
            android.R.layout.simple_list_item_1,
            android.R.id.text1,
            deviceNameList
        )

        //(C) adımı
        listview.setAdapter(veriAdaptoru)
        listview.onItemClickListener = object : OnItemClickListener {

            override fun onItemClick(
                parent: AdapterView<*>, view: View, position: Int,
                id: Long
            ) {

                pairDevice(deviceList.get(position))
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        // Don't forget to unregister the ACTION_FOUND receiver.

        bluetoothAdapter?.cancelDiscovery()
        unregisterReceiver(receiver)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQUEST_ENABLE_BT) {
            val pairedDevices: Set<BluetoothDevice>? = bluetoothAdapter?.bondedDevices
            pairedDevices?.forEach { device ->
                val deviceName = device.name
                val deviceHardwareAddress = device.address // MAC address
            }
        }
    }

    // Create a BroadcastReceiver for ACTION_FOUND.
    private val receiver = object : BroadcastReceiver() {

        override fun onReceive(context: Context, intent: Intent) {
            val action: String = intent.action
            when (action) {
                BluetoothDevice.ACTION_FOUND -> {
                    // Discovery has found a device. Get the BluetoothDevice
                    // object and its info from the Intent.
                    val device: BluetoothDevice =
                        intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
                    val deviceName = device.name
                    val deviceHardwareAddress = device.address // MAC address
                    if (deviceName != null) {

                        deviceNameList.add(deviceName)
                        deviceList.add(device)
                    }

                }
            }
            veriAdaptoru = ArrayAdapter<String>(
                this@MainActivity,
                android.R.layout.simple_list_item_1,
                android.R.id.text1,
                deviceNameList
            )
            listview.adapter = veriAdaptoru
            veriAdaptoru!!.notifyDataSetChanged()
        }
    }

    private val mPairReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val action = intent.action

            if (BluetoothDevice.ACTION_BOND_STATE_CHANGED == action) {
                val state = intent.getIntExtra(BluetoothDevice.EXTRA_BOND_STATE, BluetoothDevice.ERROR)
                val prevState = intent.getIntExtra(BluetoothDevice.EXTRA_PREVIOUS_BOND_STATE, BluetoothDevice.ERROR)

                if (state == BluetoothDevice.BOND_BONDED && prevState == BluetoothDevice.BOND_BONDING) {
                    Log.i("MainActivity", "Paired")
                } else if (state == BluetoothDevice.BOND_NONE && prevState == BluetoothDevice.BOND_BONDED) {
                    Log.i("MainActivity", "UnPaired")
                }

            }
        }
    }

    private fun pairDevice(device: BluetoothDevice) {
        try {
            val method = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                //device.createBond()
                Log.i("MainActivity", "Pair Device: " + device.createBond())
            } else {
                TODO("VERSION.SDK_INT < KITKAT")
            }
            //method.invoke(device, null as Array<Any>?)
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

//    private fun unpairDevice(device: BluetoothDevice) {
//        try {
//            val m = device.javaClass
//                .getMethod("removeBond", *(null as Array<Class<*>>?)!!)
//            m.invoke(device, null as Array<Any>?)
//        } catch (e: Exception) {
//            Log.i("MainActivity", "unpairDevice Error: " + e.message)
//        }
//
//    }

    private fun unpairDevice(device: BluetoothDevice) {
        try {
            //val method = device.javaClass.getMethod("removeBond", *(null as Array<Class<*>>?)!!)
            //method.invoke(device, null as Array<Any>?)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                device.setPairingConfirmation(true)
            }

        } catch (e: Exception) {
            Log.i("MainActivity", "unpairDevice Error: " + e.message)
        }

    }

    private inner class ConnectThread(device: BluetoothDevice?) : Thread() {

        //val MY_UUID: String = UUID.randomUUID().toString()

        val device: BluetoothDevice? = device

        lateinit var mmSocket: BluetoothSocket

        public override fun run() {
            // Cancel discovery because it otherwise slows down the connection.
            bluetoothAdapter?.cancelDiscovery()
            if (device != null) {
                mmSocket = device.createRfcommSocketToServiceRecord(MY_UUID)

            }


            mmSocket?.use { socket ->
                // Connect to the remote device through the socket. This call blocks
                // until it succeeds or throws an exception.
                socket.connect()
                val mmout = socket.outputStream
                val toSend = "Hello World!".toByteArray()
                mmout.write(toSend)
                mmout.flush()
                cancel()

                // The connection attempt succeeded. Perform work associated with
                // the connection in a separate thread.
                //manageMyConnectedSocket(socket)
                //var a:Handler= Handler()
                //var manageMyBluetoothService : MyBluetoothService = MyBluetoothService(a)
                //a.run {

                //}
                //manageMyBluetoothService.ConnectedThread(socket).run()
            }
        }

        // Closes the client socket and causes the thread to finish.
        fun cancel() {
            try {
                mmSocket?.close()
            } catch (e: IOException) {
                Log.e("MainActivity", "Could not close the client socket", e)
            }
        }
    }

// ... (Add other message types here as needed.)

    public class ConnectedThread(private val mmSocket: BluetoothSocket) : Thread() {

        private val mmInStream: InputStream = mmSocket.inputStream
        private val mmOutStream: OutputStream = mmSocket.outputStream
        private val mmBuffer: ByteArray = ByteArray(1024) // mmBuffer store for the stream

        override fun run() {
            var numBytes: Int // bytes returned from read()

            // Keep listening to the InputStream until an exception occurs.
            while (true) {
                // Read from the InputStream.
                numBytes = try {
                    mmInStream.read(mmBuffer)
                } catch (e: IOException) {
                    Log.d("MainActivity", "Input stream was disconnected", e)
                    break
                }

                // Send the obtained bytes to the UI activity.
                //val readMsg = handler.obtainMessage(
                //    MESSAGE_READ, numBytes, -1,
                //    mmBuffer
                //)
                //readMsg.sendToTarget()
            }
        }

        // Call this from the main activity to send data to the remote device.
        fun write(bytes: ByteArray) {
            try {
                mmOutStream.write(bytes)
            } catch (e: IOException) {
                Log.e("MainActivity", "Error occurred when sending data", e)

                // Send a failure message back to the activity.
                //val writeErrorMsg = handler.obtainMessage(MESSAGE_TOAST)
                //val bundle = Bundle().apply {
                //    putString("toast", "Couldn't send data to the other device")
                //}
                //writeErrorMsg.data = bundle
                //handler.sendMessage(writeErrorMsg)
                return
            }

            // Share the sent message with the UI activity.
            //val writtenMsg = handler.obtainMessage(
            //    MESSAGE_WRITE, -1, -1, mmBuffer
            //)
            //writtenMsg.sendToTarget()
        }

        // Call this method from the main activity to shut down the connection.
        fun cancel() {
            try {
                mmSocket.close()
            } catch (e: IOException) {
                Log.e("MainActivity", "Could not close the connect socket", e)
            }
        }
    }

    private inner class AcceptThread : Thread() {

        private var serverSocket: BluetoothServerSocket?

        init {
            var tmp: BluetoothServerSocket? = null
            try {
                tmp = bluetoothAdapter?.listenUsingInsecureRfcommWithServiceRecord("BluetoothCommunication", MY_UUID)
            } catch (ex: IOException) {
                ex.printStackTrace()
            }

            serverSocket = tmp
        }

        private val mmServerSocket: BluetoothServerSocket? by lazy(LazyThreadSafetyMode.NONE) {
            bluetoothAdapter?.listenUsingInsecureRfcommWithServiceRecord("Name", MY_UUID)
        }

        override fun run() {

            var socket: BluetoothSocket
            name = "AcceptThread"
            var bluetoothSocket:BluetoothSocket
            while (bluetoothAdapter!!.state !=  STATE_CONNECTED){

                socket = serverSocket?.accept()!!
                if(socket != null){

                    Log.i("MainActivity","server init")
                    continue
                }

                Log.i("MainActivity","server socket is null")

            }
        }

        // Closes the connect socket and causes the thread to finish.
        fun cancel() {
            try {
                mmServerSocket?.close()
            } catch (e: IOException) {
                Log.e(TAG, "Could not close the connect socket", e)
            }
        }
    }
}

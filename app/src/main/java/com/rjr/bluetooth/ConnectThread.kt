package com.rjr.bluetooth

import android.bluetooth.BluetoothDevice
import android.util.Log
import java.io.*
import java.util.UUID

/**
 * Created by Administrator on 2018/5/15.
 */
class ConnectThread(private val mDevice: BluetoothDevice) : Thread() {
    
    private var BT_UUID = "00001101-0000-1000-8000-00805F9B34FB" // uuid

    private var btIs: InputStream? = null
    private var btOs: OutputStream? = null

    override fun run() {
        super.run()

        // 不安全
        val bluetoothSocket = mDevice.createInsecureRfcommSocketToServiceRecord(UUID.fromString(BT_UUID))
        // 发起请求连接
        bluetoothSocket.connect()
        sendHandlerMessage("连接" + mDevice.name + "成功")
        btIs = bluetoothSocket.inputStream
        btOs = bluetoothSocket.outputStream
        val br = BufferedReader(InputStreamReader(btIs))
        val content = br.readLine()
        sendHandlerMessage("收到信息：" + content)
    }

    private fun sendHandlerMessage(message: String) {
        Log.e("rong", message)
    }

    fun write(message: String) {
        if (btOs == null) return
        val bw = BufferedWriter(OutputStreamWriter(btOs, "UTF-8"))
        bw.write(message)
        bw.close()
    }
}
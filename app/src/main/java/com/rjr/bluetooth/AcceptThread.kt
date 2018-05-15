package com.rjr.bluetooth

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothServerSocket
import android.bluetooth.BluetoothSocket
import android.util.Log
import java.io.*
import java.util.*

/**
 * Created by Administrator on 2018/5/14.
 */
class AcceptThread() : Thread() {

    private var BT_UUID = "00001101-0000-1000-8000-00805F9B34FB" // uuid
    private var canAccept = false
    private var canReceive = false
    private var adapter: BluetoothAdapter? = null

    constructor(adapter: BluetoothAdapter) : this() {
        this.adapter = adapter
        canAccept = true
        canReceive = true
    }

    private var btOs: OutputStream? = null

    private var btIs: InputStream? = null

    override fun run() {
        super.run()
        // 获取套接字
        val bluetoothServerSocket = adapter!!.listenUsingInsecureRfcommWithServiceRecord("TEST", UUID.fromString(BT_UUID))
        var socket: BluetoothSocket
        // 监听连接请求
        while (canAccept) {
            // 阻塞等待客户端连接
            socket = bluetoothServerSocket.accept()
            sendHandlerMessage("有客户端连接")
            // 获取输入输出流
            btIs = socket.inputStream
            btOs = socket.outputStream
            val br = BufferedReader(InputStreamReader(btIs))
            val content = br.readLine()
            sendHandlerMessage("收到消息：" + content)
        }
//        socket.close()
//        br.close()
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
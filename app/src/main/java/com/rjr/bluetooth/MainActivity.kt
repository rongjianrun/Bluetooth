package com.rjr.bluetooth

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.*
import java.util.ArrayList

class MainActivity : AppCompatActivity() {

    private var mContext: Context? = null

    private var connectStatusTv: TextView? = null
    private var sendStatusTv: TextView? = null
    private var listView: ListView? = null

    private var btAdapter: BluetoothAdapter? = null

    private var btReceiver: BlueToothReceiver? = null

    private var data: MutableList<BluetoothDevice>? = null

    private var adapter: DeviceAdapter? = null

    private lateinit var connectThread: ConnectThread

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mContext = this

        connectStatusTv = findViewById(R.id.tv_connect_status)
        sendStatusTv = findViewById(R.id.tv_send_status)
        listView = findViewById(R.id.lv)

        initBlueToothAdapter()
        registerBTReceiver()

        data = ArrayList()

        adapter = DeviceAdapter(data as ArrayList<BluetoothDevice>)

        listView!!.adapter = adapter

        listView!!.onItemClickListener = AdapterView.OnItemClickListener { parent, view, position, id ->
            connectThread = ConnectThread(data!![position])
            connectThread.start()
            Log.e("rong", "click" + position)
        }
    }

    /**
     * 注册监听连接状态广播
     */
    private fun registerBTReceiver() {
        btReceiver = BlueToothReceiver()
        val intentFilter = IntentFilter()
        intentFilter.addAction(BluetoothDevice.ACTION_FOUND)
        intentFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED)
        intentFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)
        registerReceiver(btReceiver, intentFilter)
    }

    private fun initBlueToothAdapter() {
        if (btAdapter == null) {
            btAdapter = BluetoothAdapter.getDefaultAdapter()
        }
    }

    private lateinit var acceptThread: AcceptThread

    fun openBT(view: View) {
        if (btAdapter == null) {
            // 设备不支持蓝牙
            Toast.makeText(this, "设备不支持蓝牙", Toast.LENGTH_SHORT).show()
            return
        }
        acceptThread = AcceptThread(btAdapter!!)
        acceptThread.start()
        if (!btAdapter!!.isEnabled) {
            // 没打开蓝牙请求
            val intent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            startActivityForResult(intent, 100)
        }
    }

    fun searchDevice(view: View) {
        btAdapter?.startDiscovery()
    }

    fun sendMsg(view: View) {
        if (connectThread != null) {
            connectThread.write("test connect")
        } else if (acceptThread != null) {
            acceptThread.write("test accept")
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(btReceiver)
    }

    inner class BlueToothReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val action = intent?.action
            when (action) {
                BluetoothDevice.ACTION_FOUND -> {
                    val device = intent.getParcelableExtra<BluetoothDevice>(BluetoothDevice.EXTRA_DEVICE)
                    Log.d("rong", "found : " + device?.name)
                    if (device?.bondState != BluetoothDevice.BOND_BONDED) {
                        // 避免重复添加已经绑定过的设备
                        data!!.add(device)
                        adapter!!.notifyDataSetChanged()
                    }
                }
                BluetoothAdapter.ACTION_DISCOVERY_STARTED -> {
                    Log.d("rong", "start")
                }
                BluetoothAdapter.ACTION_DISCOVERY_FINISHED -> {
                    Log.d("rong", "finish")
                    if (btAdapter!!.isDiscovering) {
                        btAdapter!!.cancelDiscovery()
                    }
                }
                else -> {

                }
            }
        }
    }

    inner class DeviceAdapter(private val list: ArrayList<BluetoothDevice>) : BaseAdapter() {

        override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
            var convertView = convertView
            val vh: ViewHolder
            if (convertView == null) {
                convertView = layoutInflater.inflate(R.layout.item, parent, false)
                vh = ViewHolder()
                vh.nameTv = convertView.findViewById(R.id.tv_name)
                vh.idTv = convertView.findViewById(R.id.tv_id)
                convertView.tag = vh
            } else {
                vh = convertView.tag as ViewHolder
            }
            vh.nameTv!!.text = String.format("名字：%s", list.get(position).name)
            vh.idTv!!.text = String.format("地址：%s", list.get(position).address)
            return convertView!!
        }

        override fun getItem(position: Int): Any {
            return list.get(position)
        }

        override fun getItemId(position: Int): Long {
            return position.toLong()
        }

        override fun getCount(): Int {
            return list.size
        }

    }

    class ViewHolder {
        var nameTv: TextView? = null
        var idTv: TextView? = null
    }

}

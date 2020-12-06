package tk.munditv.mtvservice

import android.app.Notification
import android.app.Service
import android.content.*
import android.os.*
import android.util.Log
import android.widget.Toast
import org.fourthline.cling.android.AndroidUpnpService
import org.fourthline.cling.android.AndroidUpnpServiceImpl
import org.fourthline.cling.model.meta.LocalDevice
import org.fourthline.cling.model.meta.RemoteDevice
import org.fourthline.cling.registry.DefaultRegistryListener
import org.fourthline.cling.registry.Registry
import tk.munditv.libtvservice.dmp.DeviceItem
import tk.munditv.libtvservice.util.ConfigData
import tk.munditv.libtvservice.util.NetworkData
import tk.munditv.libtvservice.util.Packages
import tk.munditv.mtvservice.activity.SettingActivity
import tk.munditv.mtvservice.dmr.MOSMediaRenderer
import java.net.Inet4Address
import java.net.InetAddress
import java.net.NetworkInterface
import java.net.SocketException
import java.util.*

class MainService : Service() {
    private var mContext: Context? = null
    private var hostName: String? = null
    private var hostAddress: String? = null
    private var upnpService: AndroidUpnpService? = null
    private var deviceListRegistryListener: DeviceListRegistryListener? = null
    private val mDevList: ArrayList<DeviceItem>? = ArrayList()
    private val mDmrList: ArrayList<DeviceItem>? = ArrayList()
    private val mHandle: Handler = object : Handler() {
        override fun handleMessage(msg: Message) {
            when (msg.what) {
                GET_IP_FAIL -> {
                    Toast.makeText(mContext, R.string.ip_get_fail, Toast.LENGTH_SHORT)
                }
                GET_IP_SUC -> {
                    if (null != msg.obj) {
                        val inetAddress = msg.obj as InetAddress
                        if (null != inetAddress) {
                            setIp(inetAddress)
                            setIpInfo()
                        }
                    } else {
                        Toast.makeText(mContext, R.string.ip_get_fail, Toast.LENGTH_SHORT)
                    }
                }
            }
            super.handleMessage(msg)
        }
    }
    private val ip: Unit
        private get() {
            Thread(Runnable {
                Log.d(TAG, "getIp() Thread inside : run()")
                val message = Message()
                val inetAddress = localIpAddress
                if (inetAddress != null) {
                    hostName = inetAddress.hostName
                    hostAddress = inetAddress.hostAddress
                    message.obj = inetAddress
                    message.what = GET_IP_SUC
                    mHandle.sendMessage(message)
                }
                return@Runnable
            }).start()
        }

    private fun setIp(inetAddress: InetAddress) {
        Log.d(TAG, "setIp()")
        NetworkData.setLocalIpAddress(inetAddress)
    }

    private fun setIpInfo() {
        Log.d(TAG, "setIpInfo()")
        Log.d(TAG, "hostName = $hostName")
        Log.d(TAG, "hostAddress = $hostAddress")
        NetworkData.setHostName(hostName)
        NetworkData.setHostAddress(hostAddress)
    }

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "onCreate()")
        mContext = this
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForeground(1, Notification())
        }
    }

    override fun onBind(intent: Intent): IBinder? {
        Log.d(TAG, "onBind()")
        return null
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        Log.d(TAG, "onStartCommand(intent, $flags, $startId)")
        initApps(this)
        ip
        deviceListRegistryListener = DeviceListRegistryListener()
        applicationContext.bindService(
                Intent(this, AndroidUpnpServiceImpl::class.java),
                serviceConnection, BIND_AUTO_CREATE)
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "onDestroy()")
        if (upnpService != null) {
            upnpService!!.registry
                    .removeListener(deviceListRegistryListener)
        }
        applicationContext.unbindService(serviceConnection)
    }

    protected fun searchNetwork() {
        Log.d(TAG, "searchNetwork()")
        if (upnpService == null) return
        Toast.makeText(this, R.string.searching_lan, Toast.LENGTH_SHORT).show()
        upnpService!!.registry.removeAllRemoteDevices()
        upnpService!!.controlPoint.search()
    }

    private val serviceConnection: ServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(className: ComponentName, service: IBinder) {
            Log.d(TAG, "onServiceConnected()")
            mDevList!!.clear()
            mDmrList!!.clear()
            upnpService = service as AndroidUpnpService
            NetworkData.setUpnpService(upnpService)
            NetworkData.setRenderName(SettingActivity.Companion.getRenderName(applicationContext))
            Log.d(TAG, "Connected to UPnP Service")
            val mediaRenderer = MOSMediaRenderer(1,
                    mContext!!)
            upnpService!!.registry.addDevice(mediaRenderer.getDevice())
            deviceListRegistryListener!!.dmrAdded(DeviceItem(
                    mediaRenderer.getDevice()))
            Log.d(TAG, "Local DMR Service started!")

            // xgf
            for (device in upnpService!!.registry.devices) {
                if (device.type.namespace == "schemas-upnp-org" && device.type.type == "MediaServer") {
                    val display = DeviceItem(device, device
                            .details.friendlyName,
                            device.displayString, "(REMOTE) "
                            + device.type.displayString)
                    deviceListRegistryListener!!.deviceAdded(display)
                }
            }

            // Getting ready for future device advertisements
            upnpService!!.registry.addListener(deviceListRegistryListener)
            // Refresh device list
            upnpService!!.controlPoint.search()

            // select first device by default
            if (null != mDevList && mDevList.size > 0 && null == NetworkData.getDmsDeviceItem()) {
                NetworkData.setDmsDeviceItem(mDevList[0])
            }
            if (null != mDmrList && mDmrList.size > 0 && null == NetworkData.getDmrDeviceItem()) {
                NetworkData.setdmrDeviceItem(mDmrList[0])
                Log.d(TAG, "DMR NAME = " + mDmrList[0].device.details.friendlyName)
            }
        }

        override fun onServiceDisconnected(className: ComponentName) {
            Log.d(TAG, "onServiceDisconnected()")
            upnpService = null
        }
    }

    inner class DeviceListRegistryListener : DefaultRegistryListener() {
        /* Discovery performance optimization for very slow Android devices! */
        override fun remoteDeviceDiscoveryStarted(registry: Registry,
                                                  device: RemoteDevice) {
            Log.d(TAG, "remoteDeviceDiscoveryStarted()")
        }

        override fun remoteDeviceDiscoveryFailed(registry: Registry,
                                                 device: RemoteDevice, ex: Exception) {
            Log.d(TAG, "remoteDeviceDiscoveryFailed()")
        }

        /*
         * End of optimization, you can remove the whole block if your Android
         * handset is fast (>= 600 Mhz)
         */
        override fun remoteDeviceAdded(registry: Registry, device: RemoteDevice) {
            Log.d(TAG, "remoteDeviceAdded:" + device.toString()
                    + device.type.type)
            if (device.type.namespace == "schemas-upnp-org" && device.type.type == "MediaServer") {
                val display = DeviceItem(device, device
                        .details.friendlyName,
                        device.displayString, "(REMOTE) "
                        + device.type.displayString)
                deviceAdded(display)
            }
            if (device.type.namespace == "schemas-upnp-org" && device.type.type == "MediaRenderer") {
                val dmrDisplay = DeviceItem(device, device
                        .details.friendlyName,
                        device.displayString, "(REMOTE) "
                        + device.type.displayString)
                dmrAdded(dmrDisplay)
            }
        }

        override fun remoteDeviceRemoved(registry: Registry, device: RemoteDevice) {
            Log.d(TAG, "remoteDeviceRemoved:" + device.toString()
                    + device.type.type)
            val display = DeviceItem(device,
                    device.displayString)
            deviceRemoved(display)
            if (device.type.namespace == "schemas-upnp-org" && device.type.type == "MediaRenderer") {
                val dmrDisplay = DeviceItem(device, device
                        .details.friendlyName,
                        device.displayString, "(REMOTE) "
                        + device.type.displayString)
                dmrRemoved(dmrDisplay)
            }
        }

        override fun localDeviceAdded(registry: Registry, device: LocalDevice) {
            Log.d(TAG, "localDeviceAdded:" + device.toString()
                    + device.type.type)
            val display = DeviceItem(device, device
                    .details.friendlyName, device.displayString,
                    "(REMOTE) " + device.type.displayString)
            deviceAdded(display)
        }

        override fun localDeviceRemoved(registry: Registry, device: LocalDevice) {
            Log.d(TAG, "localDeviceRemoved:" + device.toString()
                    + device.type.type)
            val display = DeviceItem(device,
                    device.displayString)
            deviceRemoved(display)
        }

        fun deviceAdded(di: DeviceItem) {
            Log.d(TAG, "deviceAdded()")
            if (!mDevList!!.contains(di)) {
                mDevList.add(di)
            }
        }

        fun deviceRemoved(di: DeviceItem) {
            Log.d(TAG, "deviceRemoved()")
            mDevList!!.remove(di)
        }

        fun dmrAdded(di: DeviceItem) {
            Log.d(TAG, "dmrAdded()")
            if (!mDmrList!!.contains(di)) {
                mDmrList.add(di)
            }
        }

        fun dmrRemoved(di: DeviceItem) {
            Log.d(TAG, "dmrRemoved()")
            mDmrList!!.remove(di)
        }
    }

    companion object {
        private val TAG = MainService::class.java.simpleName
        private const val GET_IP_FAIL = 0
        private const val GET_IP_SUC = 1
        private const val serverPrepared = false
        val localIpAddress: InetAddress?
            get() {
                try {
                    val en = NetworkInterface.getNetworkInterfaces()
                    while (en.hasMoreElements()) {
                        val intf = en.nextElement()
                        val enumIpAddr = intf.inetAddresses
                        while (enumIpAddr.hasMoreElements()) {
                            val inetAddress = enumIpAddr.nextElement()
                            if (!inetAddress.isLoopbackAddress && inetAddress is Inet4Address) {
                                return inetAddress
                            }
                        }
                    }
                } catch (ex: SocketException) {
                    ex.printStackTrace()
                }
                return null
            }

        fun initApps(context: Context?) {
            val pkg = Packages(context)
            ConfigData.apps = pkg.packages
        }
    }
}
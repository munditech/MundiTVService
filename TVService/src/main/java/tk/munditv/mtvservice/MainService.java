package tk.munditv.mtvservice;

import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;

import org.fourthline.cling.android.AndroidUpnpService;
import org.fourthline.cling.android.AndroidUpnpServiceImpl;
import org.fourthline.cling.model.meta.Device;
import org.fourthline.cling.model.meta.LocalDevice;
import org.fourthline.cling.model.meta.RemoteDevice;
import org.fourthline.cling.registry.DefaultRegistryListener;
import org.fourthline.cling.registry.Registry;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Enumeration;

import tk.munditv.libtvservice.dmp.DeviceItem;
import tk.munditv.libtvservice.util.NetworkData;
import tk.munditv.mtvservice.activity.SettingActivity;
import tk.munditv.mtvservice.application.BaseApplication;
import tk.munditv.mtvservice.dmr.MOSMediaRenderer;

public class MainService extends Service {

    private final static String TAG = MainService.class.getSimpleName();

    private static final int GET_IP_FAIL = 0;
    private static final int GET_IP_SUC = 1;

    private Context mContext;

    private String hostName;
    private String hostAddress;

    private AndroidUpnpService upnpService = null;
    private static boolean serverPrepared = false;
    private DeviceListRegistryListener deviceListRegistryListener;

    private ArrayList<DeviceItem> mDevList = new ArrayList<DeviceItem>();
    private ArrayList<DeviceItem> mDmrList = new ArrayList<DeviceItem>();

    private Handler mHandle = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case GET_IP_FAIL: {
                    Toast.makeText(mContext, R.string.ip_get_fail, Toast.LENGTH_SHORT);
                    break;
                }
                case GET_IP_SUC: {
                    if (null != msg.obj) {
                        InetAddress inetAddress = (InetAddress) msg.obj;
                        if (null != inetAddress) {
                            setIp(inetAddress);
                            setIpInfo();
                        }
                    } else {
                        Toast.makeText(mContext, R.string.ip_get_fail, Toast.LENGTH_SHORT);
                    }
                    break;
                }
            }
            super.handleMessage(msg);
        }

    };

    public static InetAddress getLocalIpAddress() {
        try {
            for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements();) {
                NetworkInterface intf = en.nextElement();
                for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements();) {
                    InetAddress inetAddress = enumIpAddr.nextElement();
                    if (!inetAddress.isLoopbackAddress() && inetAddress instanceof Inet4Address) {
                        return inetAddress;
                    }
                }
            }
        } catch (SocketException ex) {
            ex.printStackTrace();
        }
        return null;
    }

    private void getIp() {
        new Thread(new Runnable() {

            @Override
            public void run() {
                Log.d(TAG, "getIp() Thread inside : run()");
                Message message = new Message();
                InetAddress inetAddress = getLocalIpAddress();;
                if (inetAddress != null) {
                    hostName = inetAddress.getHostName();
                    hostAddress = inetAddress.getHostAddress();
                    message.obj = inetAddress;
                    message.what = GET_IP_SUC;
                    mHandle.sendMessage(message);
                }
                return;
            }
        }).start();
    }

    private void setIp(InetAddress inetAddress) {
        Log.d(TAG, "setIp()");
        BaseApplication.setLocalIpAddress(inetAddress);
    }

    private void setIpInfo() {
        Log.d(TAG, "setIpInfo()");
        Log.d(TAG, "hostName = " + hostName);
        Log.d(TAG, "hostAddress = " + hostAddress);
        BaseApplication.setHostName(hostName);
        BaseApplication.setHostAddress(hostAddress);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreate()");
        mContext = this;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        Log.d(TAG, "onBind()");
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand(intent, "+ flags + ", " + startId + ")");
        BaseApplication.initApps(this);
        getIp();
        deviceListRegistryListener = new DeviceListRegistryListener();
        getApplicationContext().bindService(
                new Intent(this, AndroidUpnpServiceImpl.class),
                serviceConnection, Context.BIND_AUTO_CREATE);
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy()");
        if (upnpService != null) {
            upnpService.getRegistry()
                    .removeListener(deviceListRegistryListener);
        }
        getApplicationContext().unbindService(serviceConnection);
    }

    protected void searchNetwork() {
        Log.d(TAG, "searchNetwork()");

        if (upnpService == null)
            return;
        Toast.makeText(this, R.string.searching_lan, Toast.LENGTH_SHORT).show();
        upnpService.getRegistry().removeAllRemoteDevices();
        upnpService.getControlPoint().search();
    }

    private ServiceConnection serviceConnection = new ServiceConnection() {

        public void onServiceConnected(ComponentName className, IBinder service) {
            Log.d(TAG, "onServiceConnected()");

            mDevList.clear();
            mDmrList.clear();

            upnpService = (AndroidUpnpService) service;
            BaseApplication.setUpnpService(upnpService);
            NetworkData.setRenderName(SettingActivity.getRenderName(getApplicationContext()));

            Log.d(TAG, "Connected to UPnP Service");

            MOSMediaRenderer mediaRenderer = new MOSMediaRenderer(1,
                    mContext);
            upnpService.getRegistry().addDevice(mediaRenderer.getDevice());
            deviceListRegistryListener.dmrAdded(new DeviceItem(
                    mediaRenderer.getDevice()));
            Log.d(TAG, "Local DMR Service started!");

            // xgf
            for (Device device : upnpService.getRegistry().getDevices()) {
                if (device.getType().getNamespace().equals("schemas-upnp-org")
                        && device.getType().getType().equals("MediaServer")) {
                    final DeviceItem display = new DeviceItem(device, device
                            .getDetails().getFriendlyName(),
                            device.getDisplayString(), "(REMOTE) "
                            + device.getType().getDisplayString());
                    deviceListRegistryListener.deviceAdded(display);
                }
            }

            // Getting ready for future device advertisements
            upnpService.getRegistry().addListener(deviceListRegistryListener);
            // Refresh device list
            upnpService.getControlPoint().search();

            // select first device by default
            if (null != mDevList && mDevList.size() > 0
                    && null == BaseApplication.deviceItem) {
                BaseApplication.deviceItem = mDevList.get(0);
            }
            if (null != mDmrList && mDmrList.size() > 0
                    && null == BaseApplication.dmrDeviceItem) {
                BaseApplication.dmrDeviceItem = mDmrList.get(0);
                NetworkData.setdmrDeviceItem(mDmrList.get(0));
                Log.d(TAG, "DMR NAME = " + mDmrList.get(0).getDevice().getDetails().getFriendlyName());
            }
        }

        public void onServiceDisconnected(ComponentName className) {
            Log.d(TAG, "onServiceDisconnected()");

            upnpService = null;
        }
    };

    public class DeviceListRegistryListener extends DefaultRegistryListener {

        /* Discovery performance optimization for very slow Android devices! */

        @Override
        public void remoteDeviceDiscoveryStarted(Registry registry,
                                                 RemoteDevice device) {
            Log.d(TAG, "remoteDeviceDiscoveryStarted()");

        }

        @Override
        public void remoteDeviceDiscoveryFailed(Registry registry,
                                                final RemoteDevice device, final Exception ex) {
            Log.d(TAG, "remoteDeviceDiscoveryFailed()");

        }

        /*
         * End of optimization, you can remove the whole block if your Android
         * handset is fast (>= 600 Mhz)
         */

        @Override
        public void remoteDeviceAdded(Registry registry, RemoteDevice device) {
            Log.d(TAG,"remoteDeviceAdded:" + device.toString()
                            + device.getType().getType());

            if (device.getType().getNamespace().equals("schemas-upnp-org")
                    && device.getType().getType().equals("MediaServer")) {
                final DeviceItem display = new DeviceItem(device, device
                        .getDetails().getFriendlyName(),
                        device.getDisplayString(), "(REMOTE) "
                        + device.getType().getDisplayString());
                deviceAdded(display);
            }

            if (device.getType().getNamespace().equals("schemas-upnp-org")
                    && device.getType().getType().equals("MediaRenderer")) {
                final DeviceItem dmrDisplay = new DeviceItem(device, device
                        .getDetails().getFriendlyName(),
                        device.getDisplayString(), "(REMOTE) "
                        + device.getType().getDisplayString());
                dmrAdded(dmrDisplay);
            }
        }

        @Override
        public void remoteDeviceRemoved(Registry registry, RemoteDevice device) {
            Log.d(TAG,"remoteDeviceRemoved:" + device.toString()
                    + device.getType().getType());

            final DeviceItem display = new DeviceItem(device,
                    device.getDisplayString());
            deviceRemoved(display);

            if (device.getType().getNamespace().equals("schemas-upnp-org")
                    && device.getType().getType().equals("MediaRenderer")) {
                final DeviceItem dmrDisplay = new DeviceItem(device, device
                        .getDetails().getFriendlyName(),
                        device.getDisplayString(), "(REMOTE) "
                        + device.getType().getDisplayString());
                dmrRemoved(dmrDisplay);
            }
        }

        @Override
        public void localDeviceAdded(Registry registry, LocalDevice device) {
            Log.d(TAG,"localDeviceAdded:" + device.toString()
                            + device.getType().getType());

            final DeviceItem display = new DeviceItem(device, device
                    .getDetails().getFriendlyName(), device.getDisplayString(),
                    "(REMOTE) " + device.getType().getDisplayString());
            deviceAdded(display);
        }

        @Override
        public void localDeviceRemoved(Registry registry, LocalDevice device) {
            Log.d(TAG,"localDeviceRemoved:" + device.toString()
                            + device.getType().getType());

            final DeviceItem display = new DeviceItem(device,
                    device.getDisplayString());
            deviceRemoved(display);
        }

        public void deviceAdded(final DeviceItem di) {
            Log.d(TAG, "deviceAdded()");

            if (!mDevList.contains(di)) {
                mDevList.add(di);
            }
        }

        public void deviceRemoved(final DeviceItem di) {
            Log.d(TAG, "deviceRemoved()");

            mDevList.remove(di);
        }

        public void dmrAdded(final DeviceItem di) {
            Log.d(TAG, "dmrAdded()");

            if (!mDmrList.contains(di)) {
                mDmrList.add(di);
            }
        }

        public void dmrRemoved(final DeviceItem di) {
            Log.d(TAG, "dmrRemoved()");

            mDmrList.remove(di);
        }
    }

}

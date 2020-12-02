package tk.munditv.mcontroller.app;

import android.app.Application;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.util.Log;

import com.nostra13.universalimageloader.cache.disc.naming.Md5FileNameGenerator;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.QueueProcessingType;

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
import tk.munditv.libtvservice.util.ConfigData;
import tk.munditv.libtvservice.util.NetworkData;
import tk.munditv.libtvservice.util.PInfo;
import tk.munditv.libtvservice.util.Packages;
import tk.munditv.mcontroller.SettingsActivity;
import tk.munditv.mcontroller.dmr.MOSMediaRenderer;
import tk.munditv.mcontroller.dms.MediaServer;
import tk.munditv.mcontroller.ui.applist.DMRListCallback;

public class MainApplication extends Application {

    private static final String TAG = MainApplication.class.getSimpleName();

    public static AndroidUpnpService upnpService;
    public static Context mContext;
    private static InetAddress inetAddress;
    private static String hostAddress;
    private static String hostName;
    private static boolean serverPrepared = false;
    private DeviceListRegistryListener deviceListRegistryListener = new DeviceListRegistryListener();
    public static DeviceItem deviceItem;
    public static DeviceItem dmrDeviceItem;
    public static ArrayList<DeviceItem> mDevList = null;
    public static ArrayList<DeviceItem> mDmrList = null;
    public static boolean isLocalDmr = true;
    public static ArrayList<PInfo> apps;
    private MediaServer mediaServer;
    private static MainApplication mInstance;
    private DMRListCallback callback;

    private ServiceConnection serviceConnection = new ServiceConnection() {

        public void onServiceConnected(ComponentName className, IBinder service) {
            Log.d(TAG, "serviceConnection -- onServiceConnected()");

            if (mDevList == null) mDevList = new ArrayList<DeviceItem>();
            if (mDmrList == null) mDmrList = new ArrayList<DeviceItem>();
            mDevList.clear();
            mDmrList.clear();

            upnpService = (AndroidUpnpService) service;

            Log.d(TAG, "Connected to UPnP Service");

            if (mediaServer == null
                    && SettingsActivity.getDmsOn(mContext)) {
                Log.d(TAG, "Start Media Server!");

                try {
                    mediaServer = new MediaServer(mContext);
                    upnpService.getRegistry()
                            .addDevice(mediaServer.getDevice());
                    DeviceItem localDevItem = new DeviceItem(
                            mediaServer.getDevice());

                    deviceListRegistryListener.deviceAdded(localDevItem);
                    new Thread(new Runnable() {

                        @Override
                        public void run() {
                            //prepareMediaServer();
                        }
                    }).start();

                } catch (Exception ex) {
                    // TODO: handle exception
                    Log.v(TAG, "Creating demo device failed");
                    return;
                }
            }

            if (SettingsActivity.getRenderOn(mContext)) {
                Log.d(TAG, "Start Media Renderer!");

                MOSMediaRenderer mediaRenderer = new MOSMediaRenderer(1,
                        mContext);
                upnpService.getRegistry().addDevice(mediaRenderer.getDevice());

                deviceListRegistryListener.dmrAdded(new DeviceItem(
                        mediaRenderer.getDevice()));
            }

            // xgf
            Log.d(TAG, "Start Search Devicesr!");

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
                    && null == deviceItem) {
                deviceItem = mDevList.get(0);
            }
            if (null != mDmrList && mDmrList.size() > 0
                    && null == dmrDeviceItem) {
                dmrDeviceItem = mDmrList.get(0);
            }
        }

        public void onServiceDisconnected(ComponentName className) {
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
            if (callback != null) {
                callback.refresh(mDmrList);
            }
        }

        public void dmrRemoved(final DeviceItem di) {
            Log.d(TAG, "dmrRemoved()");

            mDmrList.remove(di);
            if (callback != null) {
                callback.refresh(mDmrList);
            }
        }
    }

    public static MainApplication getInstance() {
        return mInstance;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        mInstance = this;
        mContext = getApplicationContext();
        initImageLoader(mContext);
        getIp();
        initApps(this);
        String dmr = SettingsActivity.getRenderName(this);
        String dms = SettingsActivity.getDeviceName(this);
        NetworkData.setServerName(dms);
        NetworkData.setRenderName(dmr);
        //if (inetAddress != null) {
            bindService(
                    new Intent(this, AndroidUpnpServiceImpl.class),
                    serviceConnection, Context.BIND_AUTO_CREATE);
        //}
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        Log.d(TAG, "onDestroy()");

        if (upnpService != null) {
            upnpService.getRegistry()
                    .removeListener(deviceListRegistryListener);
        }
        unbindService(serviceConnection);
    }

    public static void initImageLoader(Context context) {
        // This configuration tuning is custom. You can tune every option, you may tune some of them,
        // or you can create default configuration by
        //  ImageLoaderConfiguration.createDefault(this);
        // method.
        Log.d(TAG, "initImageLoader()");

        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(context)
                .threadPriority(Thread.NORM_PRIORITY - 2)
                .denyCacheImageMultipleSizesInMemory()
                .discCacheFileNameGenerator(new Md5FileNameGenerator())
                .tasksProcessingOrder(QueueProcessingType.LIFO)
                .build();
        // Initialize ImageLoader with configuration.
        ImageLoader.getInstance().init(config);
    }

    public static InetAddress getLocalIpAddress() {
        try {
            for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements();) {
                NetworkInterface intf = en.nextElement();
                for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements();) {
                    InetAddress mInetAddress = enumIpAddr.nextElement();
                    if (!mInetAddress.isLoopbackAddress() && inetAddress instanceof Inet4Address) {
                        return mInetAddress;
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
                inetAddress = getLocalIpAddress();;
                if (inetAddress != null) {
                    hostName = inetAddress.getHostName();
                    hostAddress = inetAddress.getHostAddress();
                }
                return;
            }
        }).start();
    }

    public static String getHostAddress() {
        Log.d(TAG, "getHostAddress()");
        return hostAddress;
    }

    public static String getHostName() {
        Log.d(TAG, "getHostName()");
        return hostName;
    }

    public static void initApps(Context context) {
        Log.d(TAG, "initApps()");

        Packages pkg = new Packages(context);
        apps = pkg.getPackages();
        ConfigData.apps = apps;
    }

    public static Context getContext() {
        return mContext;
    }

    public void searchNetwork() {
        Log.d(TAG, "searchNetwork()");

        if (upnpService == null)
            return;
        //Toast.makeText(this, R.string.searching_lan, Toast.LENGTH_SHORT).show();
        upnpService.getRegistry().removeAllRemoteDevices();
        upnpService.getControlPoint().search();
    }

    public void setCallback(DMRListCallback callback) {
        this.callback = callback;
    }
}

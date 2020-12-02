package tk.munditv.mtvservice.application;

import android.app.Application;
import android.content.Context;

import org.fourthline.cling.android.AndroidUpnpService;
import org.fourthline.cling.support.model.DIDLContent;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.HashMap;

import tk.munditv.libtvservice.dmp.ContentItem;
import tk.munditv.libtvservice.dmp.DeviceItem;
import tk.munditv.libtvservice.util.NetworkData;
import tk.munditv.libtvservice.util.PInfo;
import tk.munditv.libtvservice.util.Packages;

import static tk.munditv.libtvservice.util.ImageUtil.initImageLoader;

public class BaseApplication extends Application {

	public static DeviceItem deviceItem;

	public DIDLContent didl;

	public static DeviceItem dmrDeviceItem;
	
	public static boolean isLocalDmr = true;

	public ArrayList<ContentItem> listMusic;

	public ArrayList<ContentItem> listPhoto;

	public ArrayList<ContentItem> listPlayMusic = new ArrayList();

	public ArrayList<ContentItem> listVideo;

	public ArrayList<ContentItem> listcontent;

	public static ArrayList<PInfo> apps;

	public HashMap<String, ArrayList<ContentItem>> map;

	// public MediaUtils mediaUtils;

	public int position;

	public static AndroidUpnpService upnpService;

	public static Context mContext;

	private static InetAddress inetAddress;

	private static String hostAddress;

	private static String hostName;

	@Override
	public void onCreate() {
		super.onCreate();
		mContext = getApplicationContext();
	    initImageLoader(getApplicationContext());
	}

	public static Context getContext() {
		return mContext;
	}

	public static void setLocalIpAddress(InetAddress inetAddr) {
		inetAddress = inetAddr;

	}

	public static InetAddress getLocalIpAddress() {
		return inetAddress;
	}

	public static String getHostAddress() {
		return hostAddress;
	}

	public static void setHostAddress(String hostAddress) {
		BaseApplication.hostAddress = hostAddress;
		NetworkData.setHostAddress(hostAddress);
	}

	public static String getHostName() {
		return hostName;
	}

	public static void setHostName(String hostName) {
		BaseApplication.hostName = hostName;
		NetworkData.setHostName(hostName);
	}

	public static void setUpnpService(AndroidUpnpService upnpService1) {
		upnpService = upnpService1;
		NetworkData.setUpnpService(upnpService);
	}

	public static void setLocalDmrFlag(boolean flag) {
		isLocalDmr = flag;
		NetworkData.setLocalDmr(flag);
	}

	public static void setDmrDeviceItem(DeviceItem dmr) {
		dmrDeviceItem = dmr;
		NetworkData.setdmrDeviceItem(dmr);
	}

	public static void setRenderName(String name) {
		NetworkData.setRenderName(name);
	}

	public static void setServerName(String name) {
		NetworkData.setServerName(name);
	}

	public static void setSlideTime(int time) {
		NetworkData.setSlideTime(time);
	}

    public static void initApps(Context context) {
		Packages pkg = new Packages(context);
		apps = pkg.getPackages();
	}
}

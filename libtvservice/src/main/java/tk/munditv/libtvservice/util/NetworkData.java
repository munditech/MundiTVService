package tk.munditv.libtvservice.util;

import org.fourthline.cling.android.AndroidUpnpService;

import java.net.InetAddress;
import java.util.ArrayList;

import tk.munditv.libtvservice.dmp.DeviceItem;

public class NetworkData {

    private static String hostName;
    private static String hostAddress;
    private static String renderName;
    private static String serverName;
    private static int slideTime;
    private static DeviceItem dmrDeviceItem;
    private static DeviceItem dmsDeviceItem;
    public static ArrayList<DeviceItem> mDevList = null;
    public static ArrayList<DeviceItem> mDmrList = null;

    private static boolean isLocalDmr = false;
    private static AndroidUpnpService upnpService;
    private static InetAddress inetAddress;

    public static void setHostName(String hostNameString) {
        hostName = hostNameString;
    }

    public static void setHostAddress(String hostAddressString) {
        hostAddress = hostAddressString;
    }

    public static void setLocalIpAddress(InetAddress inetAddr) {
        inetAddress = inetAddr;
    }

    public static void setRenderName(String renderNameString) {
        renderName = renderNameString;
    }

    public static void setServerName(String serverNameString) {
        serverName = serverNameString;
    }

    public static void setSlideTime(int slideTimeValue) {
        slideTime = slideTimeValue;
    }

    public static void setdmrDeviceItem(DeviceItem dmrItem) {
        dmrDeviceItem = dmrItem;
    }

    public static void setDmsDeviceItem(DeviceItem dmsItem) {
        dmsDeviceItem = dmsItem;
    }

    public static void setLocalDmr(Boolean dmrflag) {
        isLocalDmr = dmrflag;
    }

    public static void setUpnpService(AndroidUpnpService mupnpService) {
        upnpService = mupnpService;
    }

    public static String getHostName() {
        return hostName;
    }

    public static String getHostAddress() {
        return hostAddress;
    }

    public static InetAddress getLocalIpAddress() {
        return inetAddress;
    }

    public static String getRenderName() {
        return renderName;
    }

    public static String getServerName() {
        return serverName;
    }

    public static int getSlideTime() {
        return slideTime;
    }

    public static DeviceItem getDmrDeviceItem() {
        return dmrDeviceItem;
    }

    public static DeviceItem getDmsDeviceItem() {
        return dmsDeviceItem;
    }

    public static boolean getIsLocalDmr() {
        return isLocalDmr;
    }

    public static AndroidUpnpService getUpnpService() {
        return upnpService;
    }
}

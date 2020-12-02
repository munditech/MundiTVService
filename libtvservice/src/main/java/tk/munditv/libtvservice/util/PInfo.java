package tk.munditv.libtvservice.util;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.PixelFormat;
import android.graphics.drawable.Drawable;
import android.util.Log;

import java.io.ByteArrayOutputStream;

public class PInfo {
    private final static String TAG = PInfo.class.getSimpleName();
    private String appname = "";
    private String pname = "";
    private String versionName = "";
    private int versionCode = 0;
    private byte[] icon;
    //private ActivityInfo[] activityInfos;
    //private ProviderInfo[] providerInfos;
    //private ServiceInfo[] serviceInfos;

    public PInfo(Context context, PackageInfo packageInfo) {
        Log.d(TAG, "PInfo()");

        appname = packageInfo.applicationInfo.loadLabel(context.getPackageManager()).toString();
        pname = packageInfo.packageName;
        versionName = packageInfo.versionName;
        versionCode = packageInfo.versionCode;

        Drawable iconDrawable = packageInfo.applicationInfo.loadIcon(context.getPackageManager());
        Bitmap bitmap = drawableToBitmap(iconDrawable);
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.WEBP,25, stream);
        icon = stream.toByteArray();

/*
        activityInfos = packageInfo.activities;
        providerInfos = packageInfo.providers;
        serviceInfos = packageInfo.services;
 */
        Log.d(TAG, "app name = " + appname);
        Log.d(TAG, "package name = " + pname);
    }

    public String prettyPrint() {
        Log.d(TAG, "prettyPrint()");

        String str = appname + "\t" + pname + "\t" + versionName + "\t" + versionCode;
        return str;
    }

    public String getAppname() {
        Log.d(TAG, "getAppname()");

        return appname;
    }

    public String getPName() {
        Log.d(TAG, "getPName()");

        return pname;
    }

    public String getVersionName() {
        Log.d(TAG, "getVersionName()");

        return versionName;
    }

    public int getVersionCode() {
        Log.d(TAG, "getVersionCode()");

        return versionCode;
    }

    public byte[] getIcon() {
        Log.d(TAG, "getIcon()");
        return icon;
    }
/*
    public ActivityInfo[] getActivityInfos() {
        return activityInfos;
    }

    public ProviderInfo[] getProviderInfos() {
        return providerInfos;
    }

    public ServiceInfo[] getServiceInfos() {
        return serviceInfos;
    }
*/
    public static Bitmap drawableToBitmap(Drawable drawable) {
        Log.d(TAG, "drawableToBitmap()");
        // 取 drawable 的长宽
        int w = drawable.getIntrinsicWidth();
        int h = drawable.getIntrinsicHeight();

        // 取 drawable 的颜色格式
        Bitmap.Config config = drawable.getOpacity() != PixelFormat.OPAQUE ? Bitmap.Config.ARGB_8888
                : Bitmap.Config.RGB_565;
        // 建立对应 bitmap
        Bitmap bitmap = Bitmap.createBitmap(w, h, config);
        // 建立对应 bitmap 的画布
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, w, h);
        // 把 drawable 内容画到画布中
        drawable.draw(canvas);
        return bitmap;
    }
}

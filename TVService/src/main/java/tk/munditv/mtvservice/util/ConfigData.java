
package tk.munditv.mtvservice.util;

import java.util.ArrayList;

import tk.munditv.mtvservice.dmp.ContentItem;

public class ConfigData {
    public static int photoPosition = 0;
    public static int videoPosition = 0;
    public static int audioPosition = 0;
    public static ArrayList<ContentItem> listPhotos = new ArrayList<ContentItem>();
    public static ArrayList<ContentItem> listVideos = new ArrayList<ContentItem>();
    public static ArrayList<ContentItem> listAudios = new ArrayList<ContentItem>();

    public static ArrayList<PInfo> apps = new ArrayList<PInfo>();
}

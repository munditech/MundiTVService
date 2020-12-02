package tk.munditv.mcontroller.ui.applist;

import java.util.ArrayList;

import tk.munditv.mcontroller.dmp.DeviceItem;

public interface DMRListCallback {
    void refresh(ArrayList<DeviceItem> deviceItems);
}

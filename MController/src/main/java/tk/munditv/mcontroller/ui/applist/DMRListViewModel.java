package tk.munditv.mcontroller.ui.applist;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.ArrayList;

import tk.munditv.libtvservice.dmp.DeviceItem;
import tk.munditv.mcontroller.app.MainApplication;

public class DMRListViewModel extends ViewModel
        implements DMRListCallback {

    private final static String TAG = DMRListViewModel.class.getSimpleName();

    private MutableLiveData<ArrayList<String>> mDmrLists;
    private ArrayList<String> lists;

    public DMRListViewModel() {
        Log.d(TAG, "DMRListViewModel()");
        mDmrLists = new MutableLiveData<>();
        MainApplication.getInstance().setCallback(this);
        if (MainApplication.mDmrList != null)
            refresh(MainApplication.mDmrList);
        return;
    }

    public LiveData<ArrayList<String>> getDMRLists() {
        Log.d(TAG, "getDMRLists()");

        return mDmrLists;
    }

    @Override
    public void refresh(ArrayList<DeviceItem> dmrlists) {
        Log.d(TAG, "refresh() dmrlists size = " + dmrlists.size());

        lists = new ArrayList<String>();
        for (int i=0; i < dmrlists.size(); i++) {
            String name = dmrlists.get(i).getDevice().getDetails().getFriendlyName();
            Log.d(TAG, "dmr(" + i + ") name = " + name);
            lists.add(name);
        }
        mDmrLists.postValue(lists);
        return;
    }
}
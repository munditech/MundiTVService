package tk.munditv.mcontroller.ui.applist;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.ArrayList;

import tk.munditv.libtvservice.util.AppListListener;
import tk.munditv.libtvservice.util.PInfo;

public class AppListViewModel extends ViewModel
    implements AppListListener {

    private final static String TAG = AppListViewModel.class.getSimpleName();

    private MutableLiveData<ArrayList<PInfo>> mAppListData;

    public AppListViewModel() {
        Log.d(TAG, "AppListViewModel()");
        mAppListData = new MutableLiveData<>();
    }

    public LiveData<ArrayList<PInfo>> getAppList() {
        Log.d(TAG, "getAppList()");
        return mAppListData;
    }

    @Override
    public void refresh(ArrayList<PInfo> lists) {
        Log.d(TAG, "refresh()");
        mAppListData.postValue(lists);
    }

}
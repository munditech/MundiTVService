package tk.munditv.mcontroller.ui.applist;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


import tk.munditv.mcontroller.dmc.DMCControl;
import tk.munditv.mcontroller.R;
import tk.munditv.mcontroller.app.MainApplication;
import tk.munditv.mcontroller.view.AppListAdapter;

public class AppListFragment extends Fragment {

    private final static String TAG = AppListFragment.class.getSimpleName();

    private AppListViewModel appListViewModel;
    private DMRListViewModel dmrListViewModel;
    private Spinner DMRSpinner;
    private ArrayAdapter DMRAdapter;
    private DMCControl dmcControl;
    private RecyclerView appListView;
    private AppListAdapter appListAdapter;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView()");

        View root = inflater.inflate(R.layout.fragment_applist, container, false);
        DMRSpinner = root.findViewById(R.id.dmr_devices);
        DMRAdapter = new ArrayAdapter(getContext(), android.R.layout.simple_spinner_item);
        DMRSpinner.setAdapter(DMRAdapter);
        dmrListViewModel =
                new ViewModelProvider(this).get(DMRListViewModel.class);
        dmrListViewModel.getDMRLists().observe(getViewLifecycleOwner(),
                lists -> {
                    Log.d(TAG, "onChanged()");

                    DMRAdapter.clear();
                    DMRAdapter.addAll(lists);
                    DMRAdapter.notifyDataSetChanged();
                });
        DMRSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Log.d(TAG, "onItemSelected()");

                MainApplication.dmrDeviceItem = MainApplication.mDmrList.get(position);
                dmcControl = new DMCControl(null,
                        3, MainApplication.dmrDeviceItem,
                        MainApplication.upnpService,
                        null, null,  appListViewModel);
                dmcControl.getPackages();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                Log.d(TAG, "onNothingSelected()");

                parent.setSelection(0);
                MainApplication.dmrDeviceItem = MainApplication.mDmrList.get(0);
                dmcControl = new DMCControl(null,
                        3, MainApplication.dmrDeviceItem,
                        MainApplication.upnpService,
                        null, null, appListViewModel);
                dmcControl.getPackages();
            }
        });

        appListViewModel =
                new ViewModelProvider(this).get(AppListViewModel.class);
        appListViewModel.getAppList().observe(getViewLifecycleOwner(),
                pInfos -> {
                    if (pInfos.size() > 0) {
                        appListView = root.findViewById(R.id.applist_view);
                        appListAdapter = new AppListAdapter(getContext(), pInfos);
                        RecyclerView.LayoutManager layoutManager
                                = new LinearLayoutManager(getContext());
                        appListView.setLayoutManager(layoutManager);
                        appListView.setAdapter(appListAdapter);
                    }
                    return;
                });

        return root;
    }

}
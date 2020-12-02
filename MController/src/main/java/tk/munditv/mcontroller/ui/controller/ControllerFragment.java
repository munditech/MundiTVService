package tk.munditv.mcontroller.ui.controller;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import tk.munditv.libtvservice.dmp.DeviceItem;
import tk.munditv.libtvservice.dmc.DMCControl;
import tk.munditv.mcontroller.R;
import tk.munditv.mcontroller.app.MainApplication;

public class ControllerFragment extends Fragment {

    private final static String TAG = ControllerFragment.class.getSimpleName();

    private DeviceItem targetDMR;
    private Button btn_0;
    private Button btn_1;
    private Button btn_2;
    private Button btn_3;
    private Button btn_4;
    private Button btn_5;
    private Button btn_6;
    private Button btn_7;
    private Button btn_8;
    private Button btn_9;
    private Button btn_tv;
    private Button btn_vod;
    private Button btn_karaoka;
    private Button btn_music;
    private Button btn_game;
    private Button btn_youtube;
    private Button btn_delete;
    private Button btn_recall;
    private Button btn_menu;
    private Button btn_back;
    private Button btn_help;
    private Button btn_guide;
    private Button btn_up;
    private Button btn_down;
    private Button btn_left;
    private Button btn_right;
    private Button btn_enter;

    private DMCControl dmcControl;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView()");

        targetDMR = MainApplication.dmrDeviceItem;
        Log.d(TAG, "targetDMR = " + targetDMR.getDevice().getDisplayString());

        View root = inflater.inflate(R.layout.fragment_controller, container, false);
        btn_0 = root.findViewById(R.id.btn_0);
        btn_1 = root.findViewById(R.id.btn_1);
        btn_2 = root.findViewById(R.id.btn_2);
        btn_3 = root.findViewById(R.id.btn_3);
        btn_4 = root.findViewById(R.id.btn_4);
        btn_5 = root.findViewById(R.id.btn_5);
        btn_6 = root.findViewById(R.id.btn_6);
        btn_7 = root.findViewById(R.id.btn_7);
        btn_8 = root.findViewById(R.id.btn_8);
        btn_9 = root.findViewById(R.id.btn_9);
        btn_tv = root.findViewById(R.id.btn_tv);
        btn_vod = root.findViewById(R.id.btn_vod);
        btn_karaoka = root.findViewById(R.id.btn_karaoka);
        btn_music = root.findViewById(R.id.btn_music);
        btn_game = root.findViewById(R.id.btn_game);
        btn_youtube = root.findViewById(R.id.btn_youtube);
        btn_delete = root.findViewById(R.id.btn_delete);
        btn_back = root.findViewById(R.id.btn_back);
        btn_menu = root.findViewById(R.id.btn_menu);
        btn_recall = root.findViewById(R.id.btn_recall);
        btn_help = root.findViewById(R.id.btn_help);
        btn_guide = root.findViewById(R.id.btn_guide);
        btn_up = root.findViewById(R.id.btn_up);
        btn_down = root.findViewById(R.id.btn_down);
        btn_left = root.findViewById(R.id.btn_left);
        btn_right = root.findViewById(R.id.btn_right);
        btn_enter = root.findViewById(R.id.btn_enter);

        btn_0.setOnClickListener(listener);
        btn_1.setOnClickListener(listener);
        btn_2.setOnClickListener(listener);
        btn_3.setOnClickListener(listener);
        btn_4.setOnClickListener(listener);
        btn_5.setOnClickListener(listener);
        btn_6.setOnClickListener(listener);
        btn_7.setOnClickListener(listener);
        btn_8.setOnClickListener(listener);
        btn_9.setOnClickListener(listener);
        btn_tv.setOnClickListener(listener);
        btn_vod.setOnClickListener(listener);
        btn_karaoka.setOnClickListener(listener);
        btn_delete.setOnClickListener(listener);
        btn_back.setOnClickListener(listener);
        btn_left.setOnClickListener(listener);
        btn_right.setOnClickListener(listener);
        btn_up.setOnClickListener(listener);
        btn_down.setOnClickListener(listener);
        btn_enter.setOnClickListener(listener);
        btn_music.setOnClickListener(listener);
        btn_game.setOnClickListener(listener);
        btn_youtube.setOnClickListener(listener);
        btn_menu.setOnClickListener(listener);
        btn_recall.setOnClickListener(listener);
        btn_help.setOnClickListener(listener);
        btn_guide.setOnClickListener(listener);

        dmcControl = new DMCControl(null,
                3, MainApplication.dmrDeviceItem,
                MainApplication.upnpService, null, null, null);

        return root;
    }

    private View.OnClickListener listener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Log.d(TAG, "onClick()");
            if (dmcControl == null) return;
            switch (v.getId()) {
                case R.id.btn_0 :
                    Log.d(TAG, "button 0");
                    dmcControl.setCommand("[KEYCODE]KEY_0");
                    break;
                case R.id.btn_1 :
                    Log.d(TAG, "button 1");
                    dmcControl.setCommand("[KEYCODE]KEY_1");
                    break;
                case R.id.btn_2 :
                    Log.d(TAG, "button 2");
                    dmcControl.setCommand("[KEYCODE]KEY_2");
                    break;
                case R.id.btn_3 :
                    Log.d(TAG, "button 3");
                    dmcControl.setCommand("[KEYCODE]KEY_3");
                    break;
                case R.id.btn_4 :
                    Log.d(TAG, "button 4");
                    dmcControl.setCommand("[KEYCODE]KEY_4");
                    break;
                case R.id.btn_5 :
                    Log.d(TAG, "button 5");
                    dmcControl.setCommand("[KEYCODE]KEY_5");
                    break;
                case R.id.btn_6 :
                    Log.d(TAG, "button 6");
                    dmcControl.setCommand("[KEYCODE]KEY_6");
                    break;
                case R.id.btn_7 :
                    Log.d(TAG, "button 7");
                    dmcControl.setCommand("[KEYCODE]KEY_7");
                    break;
                case R.id.btn_8 :
                    Log.d(TAG, "button 8");
                    dmcControl.setCommand("[KEYCODE]KEY_8");
                    break;
                case R.id.btn_9 :
                    Log.d(TAG, "button 9");
                    dmcControl.setCommand("[KEYCODE]KEY_9");
                    break;
                case R.id.btn_tv :
                    Log.d(TAG, "button TV");
                    dmcControl.setCommand("[COMMAND]100TV");
                    break;
                case R.id.btn_vod :
                    Log.d(TAG, "button VOD");
                    dmcControl.setCommand("[COMMAND]QPlay");
                    break;
                case R.id.btn_karaoka :
                    Log.d(TAG, "button Karaoka");
                    dmcControl.setCommand("[COMMAND]Karaoka");
                    break;
                case R.id.btn_music :
                    Log.d(TAG, "button Music");
                    dmcControl.setCommand("[COMMAND]Music");
                    break;
                case R.id.btn_game :
                    Log.d(TAG, "button Game");
                    dmcControl.setCommand("[COMMAND]Game");
                    break;
                case R.id.btn_youtube :
                    Log.d(TAG, "button YouTube");
                    dmcControl.setCommand("[COMMAND]YouTube");
                    break;
                case R.id.btn_delete :
                    Log.d(TAG, "button Delete");
                    dmcControl.setCommand("[KEYCODE]KEY_DELETE");
                    break;
                case R.id.btn_recall :
                    Log.d(TAG, "button Recall");
                    dmcControl.setCommand("[KEYCODE]KEY_RECALL");
                    break;
                case R.id.btn_menu :
                    Log.d(TAG, "button Menu");
                    dmcControl.setCommand("[KEYCODE]KEY_MENU");
                    break;
                case R.id.btn_back :
                    Log.d(TAG, "button Back");
                    dmcControl.setCommand("[KEYCODE]KEY_BACK");
                    break;
                case R.id.btn_help :
                    Log.d(TAG, "button Help");
                    dmcControl.setCommand("[KEYCODE]KEY_HELP");
                    break;
                case R.id.btn_guide :
                    Log.d(TAG, "button Guide");
                    dmcControl.setCommand("[KEYCODE]KEY_GUIDE");
                    break;
                case R.id.btn_up :
                    Log.d(TAG, "button Up");
                    dmcControl.setCommand("[KEYCODE]KEY_DPAD_UP");
                    break;
                case R.id.btn_down :
                    Log.d(TAG, "button Down");
                    dmcControl.setCommand("[KEYCODE]KEY_DPAD_DOWN");
                    break;
                case R.id.btn_left :
                    Log.d(TAG, "button Left");
                    dmcControl.setCommand("[KEYCODE]KEY_DPAD_LEFT");
                    break;
                case R.id.btn_right :
                    Log.d(TAG, "button Right");
                    dmcControl.setCommand("[KEYCODE]KEY_DPAD_RIGHT");
                    break;
                case R.id.btn_enter :
                    Log.d(TAG, "button Enter(OK)");
                    dmcControl.setCommand("[KEYCODE]KEY_ENTER");
                    break;
            }
            return;
        }
    };


}
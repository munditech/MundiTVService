package tk.munditv.mcontroller.dmc;

import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.gson.Gson;

import org.fourthline.cling.android.AndroidUpnpService;
import org.fourthline.cling.controlpoint.ControlPoint;
import org.fourthline.cling.model.meta.Device;
import org.fourthline.cling.model.meta.Service;
import org.fourthline.cling.model.types.UDAServiceType;

import java.util.ArrayList;

import tk.munditv.libtvservice.dmp.DeviceItem;
import tk.munditv.libtvservice.util.Action;
import tk.munditv.libtvservice.util.AppListListener;
import tk.munditv.libtvservice.util.PInfo;
import tk.munditv.mcontroller.R;
import tk.munditv.mcontroller.dmc.callback.CurrentConnectionInfoCallback;
import tk.munditv.mcontroller.dmc.callback.GetCommandCallback;
import tk.munditv.mcontroller.dmc.callback.GetDeviceCapabilitiesCallback;
import tk.munditv.mcontroller.dmc.callback.GetMediaInfoCallback;
import tk.munditv.mcontroller.dmc.callback.GetMuteCallback;
import tk.munditv.mcontroller.dmc.callback.GetPackagesCallback;
import tk.munditv.mcontroller.dmc.callback.GetPositionInfoCallback;
import tk.munditv.mcontroller.dmc.callback.GetProtocolInfoCallback;
import tk.munditv.mcontroller.dmc.callback.GetTransportInfoCallback;
import tk.munditv.mcontroller.dmc.callback.GetVolumeCallback;
import tk.munditv.mcontroller.dmc.callback.PauseCallback;
import tk.munditv.mcontroller.dmc.callback.PlayerCallback;
import tk.munditv.mcontroller.dmc.callback.SeekCallback;
import tk.munditv.mcontroller.dmc.callback.SetAVTransportURIActionCallback;
import tk.munditv.mcontroller.dmc.callback.SetCommandCallback;
import tk.munditv.mcontroller.dmc.callback.SetMuteCalllback;
import tk.munditv.mcontroller.dmc.callback.SetVolumeCallback;
import tk.munditv.mcontroller.dmc.callback.StopCallback;

public class DMCControl {

	private final static String TAG = DMCControl.class.getSimpleName();

	public static final int TYPE_IMAGE = 1;
	public static final int TYPE_AUDIO = 2;
	public static final int TYPE_VIDEO = 3;
	public static final int CUT_VOC = 0;
	public static final int ADD_VOC = 1;
	public static boolean isExit = false;
	private AppCompatActivity activity;
	private AppListListener listListener;
	private int controlType = 1;
	int currentPlayPosition;
	private long currentVolume = 0L;
	private DeviceItem executeDeviceItem;
	public boolean isGetNoMediaPlay = false;
	public boolean isMute = false;
	public String commandString;
	public String packagesString;
	private String metaData;
	String relTime;
	private boolean threadGetState = false;
	int totalPlayTime;
	String trackTime;
	private AndroidUpnpService upnpService;
	private String uriString;

	private Handler mHandle = new Handler() {

		@Override
		public void handleMessage(Message msg) {

			Log.d(TAG, "handleMessage() = " + msg.what);

			switch (msg.what) {

			case DMCControlMessage.ADDVOLUME: {

				break;
			}

			case DMCControlMessage.CONNECTIONFAILED: {

				break;
			}

			case DMCControlMessage.CONNECTIONSUCESSED: {
				getTransportInfo(false);
				break;
			}

			case DMCControlMessage.GETMUTE: {
				DMCControl.this.isMute = msg.getData().getBoolean("mute");
				DMCControl.this.setMuteToActivity(isMute);
				// getMute();
				break;
			}

			case DMCControlMessage.GETMEDIA: {

				break;
			}

			case DMCControlMessage.GETPOTITION: {
				getPositionInfo();

				// TODO
				if (!isExit && controlType != TYPE_IMAGE) {
					mHandle.sendEmptyMessageDelayed(
							DMCControlMessage.GETPOTITION, 500);
				}
				break;
			}

			case DMCControlMessage.GETTRANSPORTINFO: {

				break;
			}

			case DMCControlMessage.GET_CURRENT_VOLUME: {

				break;
			}

			case DMCControlMessage.PAUSE: {

				break;
			}

			case DMCControlMessage.PLAY: {
				mHandle.sendEmptyMessageDelayed(DMCControlMessage.GETPOTITION,
						500);
				play();
				break;
			}

			case DMCControlMessage.PLAYAUDIOFAILED: {

				break;
			}

			case DMCControlMessage.PLAYIMAGEFAILED: {

				break;
			}

			case DMCControlMessage.PLAYVIDEOFAILED: {

				break;
			}

			case DMCControlMessage.PLAYMEDIAFAILED: {
				setPlayErrorMessage();
				stopGetPosition();
				break;
			}
			case DMCControlMessage.REDUCEVOLUME: {

				break;
			}

			case DMCControlMessage.REMOTE_NOMEDIA: {

				break;
			}

			case DMCControlMessage.SETMUTE: {
				isMute = msg.getData().getBoolean("mute");
				setMute(!isMute);
				break;
			}

			case DMCControlMessage.SETMUTESUC: {
				isMute = msg.getData().getBoolean("mute");
				setMuteToActivity(isMute);
				break;
			}

			case DMCControlMessage.SETURL: {
				setAvURL();
				break;
			}

			case DMCControlMessage.SETVOLUME: {
				if (msg.getData().getInt("isSetVolume") == CUT_VOC) {
					setVolume(msg.getData().getLong("getVolume"), CUT_VOC);
				} else {
					setVolume(msg.getData().getLong("getVolume"), ADD_VOC);
				}
				break;
			}

			case DMCControlMessage.STOP: {

				break;
			}

			case DMCControlMessage.UPDATE_PLAY_TRACK: {

				break;
			}

			case DMCControlMessage.GETCOMMAND: {
				DMCControl.this.commandString = msg.getData().getString("command");
				break;
			}

			case DMCControlMessage.SETCOMMAND: {
				commandString = msg.getData().getString("command");
				//setCommand(commandString);
				break;
			}

			case DMCControlMessage.SETCOMMANDSUC: {
				commandString = msg.getData().getString("command");
				//setCommand(commandString);
				break;
			}

			case DMCControlMessage.GETPACKAGES: {
				DMCControl.this.packagesString = msg.getData().getString("packages");
				//setCommand(commandString);
				gotPackages(packagesString);
				break;
			}

			}
		}
	};

	public DMCControl(AppCompatActivity paramActivity, int paramInt,
                      DeviceItem paramDeviceItem,
                      AndroidUpnpService paramAndroidUpnpService, String paramString1,
                      String paramString2, AppListListener listListener) {
		Log.d(TAG, "DMCControl()");

		this.activity = paramActivity;
		this.controlType = paramInt;
		this.executeDeviceItem = paramDeviceItem;
		this.upnpService = paramAndroidUpnpService;
		this.uriString = paramString1;
		this.metaData = paramString2;
		this.listListener = listListener;
	}

	private void setPlayErrorMessage() {
		Log.d(TAG, "setPlayErrorMessage()");

		Intent localIntent = new Intent();
		if (this.controlType == TYPE_VIDEO) {
			localIntent.setAction(Action.PLAY_ERR_VIDEO);
		} else if (this.controlType == TYPE_AUDIO) {
			localIntent.setAction(Action.PLAY_ERR_AUDIO);
		} else {
			localIntent.setAction(Action.PLAY_ERR_IMAGE);
		}
		activity.sendBroadcast(localIntent);
	}

	private void stopGetPosition() {
		Log.d(TAG, "stopGetPosition()");

		Message msg = new Message();
		msg.what = DMCControlMessage.GETPOTITION;
		msg.arg1 = 1;
		mHandle.sendMessage(msg);
	}

	public void getCurrentConnectionInfo(int paramInt) {
		Log.d(TAG, "getCurrentConnectionInfo()");

		try {
			Service localService = this.executeDeviceItem.getDevice()
					.findService(new UDAServiceType("ConnectionManager"));
			if (localService != null) {
				this.upnpService.getControlPoint().execute(
						new CurrentConnectionInfoCallback(localService,
								this.upnpService.getControlPoint(), paramInt));
			} else {
			}
		} catch (Exception localException) {
			localException.printStackTrace();
		}
	}

	public void getDeviceCapability() {
		Log.d(TAG, "getDeviceCapability()");

		try {
			Service localService = this.executeDeviceItem.getDevice()
					.findService(new UDAServiceType("AVTransport"));
			if (localService != null) {
				this.upnpService.getControlPoint().execute(
						new GetDeviceCapabilitiesCallback(localService));
			} else {
			}
		} catch (Exception localException) {
			localException.printStackTrace();
		}
	}

	public void getMediaInfo() {
		Log.d(TAG, "getMediaInfo()");

		try {
			Service localService = this.executeDeviceItem.getDevice()
					.findService(new UDAServiceType("AVTransport"));
			if (localService != null) {
				this.upnpService.getControlPoint().execute(
						new GetMediaInfoCallback(localService));
			} else {
			}
		} catch (Exception localException) {
			localException.printStackTrace();
		}
	}

	public void getMute() {
		Log.d(TAG, "getMute()");

		try {
			Service localService = this.executeDeviceItem.getDevice()
					.findService(new UDAServiceType("RenderingControl"));
			if (localService != null) {
				this.upnpService.getControlPoint().execute(
						new GetMuteCallback(localService, mHandle));
			} else {
			}
		} catch (Exception localException) {
			localException.printStackTrace();
		}
	}

	public void getCommand() {
		Log.d(TAG, "getCommand()");

		Log.d(TAG, "executeDeviceItem = " + executeDeviceItem
				.getDevice().getDetails().getFriendlyName());

		try {
			Service localService = this.executeDeviceItem.getDevice()
					.findService(new UDAServiceType("TVControl"));
			if (localService != null) {
				this.upnpService.getControlPoint().execute(
						new GetCommandCallback(localService, mHandle));
			}
		} catch (Exception localException) {
			localException.printStackTrace();
		}
	}

	public void getPackages() {
		Log.d(TAG, "getPackages()");
		Log.d(TAG, "executeDeviceItem = " + executeDeviceItem
				.getDevice().getDetails().getFriendlyName());
		try {
			Service localService = this.executeDeviceItem.getDevice()
					.findService(new UDAServiceType("TVControl"));
			if (localService != null) {
				this.upnpService.getControlPoint().execute(
						new GetPackagesCallback(localService, mHandle));
			} else {
				Log.d(TAG, "getPackages Error!");
			}
		} catch (Exception localException) {
			localException.printStackTrace();
		}
	}

	public void gotPackages(String str) {
		Log.d(TAG, "gotPackages()");

		boolean AppListDebug = true;

		try {
			PInfo[] lists = new Gson().fromJson(str, PInfo[].class);
			Log.d(TAG, "lists size : " + lists.length);
			ArrayList<PInfo> alists = new ArrayList<PInfo>();
			alists.clear();
			for (int i = 0; i < lists.length; i++) {
				if (AppListDebug) {
					Log.d(TAG, "lists(" + i + ").appname = " + lists[i].getAppname());
					Log.d(TAG, "lists(" + i + ").Pname = " + lists[i].getPName());
					Log.d(TAG, "lists(" + i + ").VersionName = " + lists[i].getVersionName());
					Log.d(TAG, "lists(" + i + ").VersionCode = " + lists[i].getVersionCode());
					Log.d(TAG, "lists(" + i + ").icon = " + lists[i].getIcon());
				}
				alists.add(lists[i]);
			}
			listListener.refresh(alists);
		} catch(Exception e) {
			e.printStackTrace();
		}
		return;
	}

	public void getPositionInfo() {
		Log.d(TAG, "getPositionInfo()");

		try {
			Service localService = this.executeDeviceItem.getDevice()
					.findService(new UDAServiceType("AVTransport"));
			if (localService != null) {
				this.upnpService.getControlPoint().execute(
						new GetPositionInfoCallback(localService, mHandle,
								this.activity));
			} else {
			}
		} catch (Exception localException) {
			localException.printStackTrace();
		}
	}

	public void getProtocolInfos(String paramString) {
		Log.d(TAG, "getProtocolInfos()");

		try {
			Service localService = this.executeDeviceItem.getDevice()
					.findService(new UDAServiceType("ConnectionManager"));
			if (localService != null) {
				this.upnpService.getControlPoint().execute(
						new GetProtocolInfoCallback(localService,
								this.upnpService.getControlPoint(),
								paramString, mHandle));
			} else {
			}
		} catch (Exception localException) {
			localException.printStackTrace();
		}
	}

	public void getTransportInfo(boolean paramBoolean) {
		Log.d(TAG, "getTransportInfo()");

		try {
			Service localService = this.executeDeviceItem.getDevice()
					.findService(new UDAServiceType("AVTransport"));
			if (localService != null) {
				this.upnpService.getControlPoint().execute(
						new GetTransportInfoCallback(localService, mHandle,
								paramBoolean, this.controlType));
			} else {
			}
		} catch (Exception localException) {
			localException.printStackTrace();
		}
	}

	public void getVolume(int paramInt) {
		Log.d(TAG, "getVolume()");

		try {
			Service localService = this.executeDeviceItem.getDevice()
					.findService(new UDAServiceType("RenderingControl"));
			if (localService != null) {
				Log.e(TAG, "get volume");
				this.upnpService.getControlPoint().execute(
						new GetVolumeCallback(this.activity, mHandle, paramInt,
								localService, this.controlType));
			} else {
				Log.e(TAG, "null");
			}
		} catch (Exception localException) {
			localException.printStackTrace();
		}
	}

	public void pause() {
		Log.d(TAG, "pause()");

		try {
			Service localService = this.executeDeviceItem.getDevice()
					.findService(new UDAServiceType("AVTransport"));
			if (localService != null) {
				Log.e(TAG, "pause");
				this.upnpService.getControlPoint().execute(
						new PauseCallback(localService));
			} else {
				Log.e(TAG, "null");
			}
		} catch (Exception localException) {
			localException.printStackTrace();
		}
	}

	public void play() {
		Log.d(TAG, "play()");

		try {
			Service localService = this.executeDeviceItem.getDevice()
					.findService(new UDAServiceType("AVTransport"));
			if (localService != null) {
				Log.e(TAG, "start play");
				this.upnpService.getControlPoint().execute(
						new PlayerCallback(localService, mHandle));
			} else {
				Log.e(TAG, "null");
			}
		} catch (Exception localException) {
			localException.printStackTrace();
		}
	}

	public void rePlayControl() {
		Log.d(TAG, "rePlayControl()");

		if (this.isGetNoMediaPlay)
			return;
		this.isGetNoMediaPlay = true;
		new Thread(new Runnable() {
			public void run() {
				try {
					Thread.sleep(2000L);
					DMCControl.this.setAvURL();
					DMCControl.this.isGetNoMediaPlay = false;
					return;
				} catch (Exception localException) {
					localException.printStackTrace();
				}
			}
		}).start();
	}

	public void seekBarPosition(String paramString) {
		Log.d(TAG, "seekBarPosition()");

		try {
			Device localDevice = this.executeDeviceItem.getDevice();
			Log.e("control action", "seekBarPosition");
			Service localService = localDevice.findService(new UDAServiceType(
					"AVTransport"));
			if (localService != null) {
				Log.e(TAG, "get seekBarPosition info");
				this.upnpService.getControlPoint().execute(
						new SeekCallback(activity, localService, paramString,
								mHandle));
			} else {
				Log.e("null", "null");
			}
		} catch (Exception localException) {
			localException.printStackTrace();
		}
	}

	public void setAvURL() {
		Log.d(TAG, "setAvURL()");

		try {
			Service localService = this.executeDeviceItem.getDevice()
					.findService(new UDAServiceType("AVTransport"));
			if (localService != null) {
				Log.e("set url", "set url" + this.uriString);
				this.upnpService.getControlPoint().execute(
						new SetAVTransportURIActionCallback(localService,
								this.uriString, this.metaData, mHandle,
								this.controlType));
			} else {
				Log.e("null", "null");
			}
		} catch (Exception localException) {
			localException.printStackTrace();
		}
	}

	public void setCurrentPlayPath(String paramString) {
		Log.d(TAG, "setCurrentPlayPath()");

		uriString = paramString;
	}

	public void setCurrentPlayPath(String paramString1, String paramString2) {
		Log.d(TAG, "setCurrentPlayPath()");

		uriString = paramString1;
		metaData = paramString2;
	}

	public void setMute(boolean paramBoolean) {
		Log.d(TAG, "setMute()");

		try {
			Service localService = this.executeDeviceItem.getDevice()
					.findService(new UDAServiceType("RenderingControl"));
			if (localService != null) {
				ControlPoint localControlPoint = this.upnpService
						.getControlPoint();
				localControlPoint.execute(new SetMuteCalllback(localService,
						paramBoolean, mHandle));
			} else {
				Log.e(TAG, "setMute Error!");
			}
		} catch (Exception localException) {
			localException.printStackTrace();
		}
	}

	public void setCommand(String paramString) {
		Log.d(TAG, "setCommand()");
		Log.d(TAG, "executeDeviceItem = " + executeDeviceItem
				.getDevice().getDetails().getFriendlyName());
		try {
			Service localService = this.executeDeviceItem.getDevice()
					.findService(new UDAServiceType("TVControl"));
			if (localService != null) {
				ControlPoint localControlPoint = this.upnpService
						.getControlPoint();
				localControlPoint.execute(new SetCommandCallback(localService,
						paramString, mHandle));
			} else {
				Log.e(TAG, "setCommand Error!");
			}
		} catch (Exception localException) {
			localException.printStackTrace();
		}
	}

	public void setMuteToActivity(boolean paramBoolean) {
		Log.d(TAG, "setMuteToActivity()");
/*
		if (activity instanceof ControlActivity) {
			((ControlActivity) activity).setVideoRemoteMuteState(paramBoolean);
		}

*/
	}

	public void setVolume(long paramLong, int paramInt) {
		Log.d(TAG, "setVolume()");

		if (paramInt == 0) {
		}
		Service localService = null;
		try {
			localService = this.executeDeviceItem.getDevice().findService(
					new UDAServiceType("RenderingControl"));
			if (localService != null) {
				if (paramInt == CUT_VOC) {
					if (paramLong >= 0L) {
						paramLong -= 1L;
					} else {
						Toast.makeText(activity, R.string.min_voc,
								Toast.LENGTH_SHORT).show();
					}
				} else {
					paramLong += 1L;
				}
				this.upnpService.getControlPoint().execute(
						new SetVolumeCallback(localService, paramLong));
			}
		} catch (Exception localException) {
			localException.printStackTrace();
		}
	}

	public void startThreadGetMessage() {
		Log.d(TAG, "startThreadGetMessage"
				+ this.threadGetState);

		DMCControlMessage.runing = true;
		if (!threadGetState)
			return;
		threadGetState = false;
		new Thread(new Runnable() {
			public void run() {
				try {
					boolean bool = DMCControlMessage.runing;
					if (!bool) {
						DMCControl.this.threadGetState = true;
						return;
					}
					Thread.sleep(1000L);
					DMCControl.this.getPositionInfo();
					// DMCControl.this.getTransportInfo(true);
				} catch (Exception localException) {
					localException.printStackTrace();
				}
			}
		}).start();
	}

	public void stop(Boolean paramBoolean) {
		Log.d(TAG, "stop()");

		try {
			Service localService = this.executeDeviceItem.getDevice()
					.findService(new UDAServiceType("AVTransport"));
			if (localService != null) {
				this.upnpService.getControlPoint().execute(
						new StopCallback(localService, mHandle, paramBoolean,
								this.controlType));
			} else {
				Log.d(TAG, "null");
			}
		} catch (Exception localException) {
			localException.printStackTrace();
		}
	}

}

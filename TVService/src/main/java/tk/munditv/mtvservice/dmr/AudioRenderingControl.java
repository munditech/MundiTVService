package tk.munditv.mtvservice.dmr;

import android.util.Log;

import org.fourthline.cling.model.types.ErrorCode;
import org.fourthline.cling.model.types.UnsignedIntegerFourBytes;
import org.fourthline.cling.model.types.UnsignedIntegerTwoBytes;
import org.fourthline.cling.support.lastchange.LastChange;
import org.fourthline.cling.support.model.Channel;
import org.fourthline.cling.support.renderingcontrol.AbstractAudioRenderingControl;
import org.fourthline.cling.support.renderingcontrol.RenderingControlErrorCode;
import org.fourthline.cling.support.renderingcontrol.RenderingControlException;

import java.util.Map;

/**
 * @author offbye
 */
public class AudioRenderingControl extends AbstractAudioRenderingControl {

    private final static String TAG = AudioRenderingControl.class.getSimpleName();

    final private Map<UnsignedIntegerFourBytes, MOSMediaPlayer> players;

    protected AudioRenderingControl(LastChange lastChange, Map<UnsignedIntegerFourBytes, MOSMediaPlayer> players) {
        super(lastChange);
        Log.d(TAG, "AudioRenderingControl()");

        this.players = players;
    }

    protected Map<UnsignedIntegerFourBytes, MOSMediaPlayer> getPlayers() {
        Log.d(TAG, "getPlayers()");
        return players;
    }

    protected MOSMediaPlayer getInstance(UnsignedIntegerFourBytes instanceId) throws RenderingControlException {
        Log.d(TAG, "getInstance()");

        MOSMediaPlayer player = getPlayers().get(instanceId);
        if (player == null) {
            throw new RenderingControlException(RenderingControlErrorCode.INVALID_INSTANCE_ID);
        }
        return player;
    }

    protected void checkChannel(String channelName) throws RenderingControlException {
        Log.d(TAG, "checkChannel()");

        if (!getChannel(channelName).equals(Channel.Master)) {
            throw new RenderingControlException(ErrorCode.ARGUMENT_VALUE_INVALID, "Unsupported audio channel: " + channelName);
        }
    }

    @Override
    public boolean getMute(UnsignedIntegerFourBytes instanceId, String channelName) throws RenderingControlException {
        Log.d(TAG, "getMute()");

        checkChannel(channelName);
        return getInstance(instanceId).getVolume() == 0;
    }

    @Override
    public void setMute(UnsignedIntegerFourBytes instanceId, String channelName, boolean desiredMute) throws RenderingControlException {
        Log.d(TAG, "setMute() = " + desiredMute);

        checkChannel(channelName);
        getInstance(instanceId).setMute(desiredMute);
    }

    @Override
    public String getCommand(UnsignedIntegerFourBytes instanceId, String channelName) throws RenderingControlException {
        Log.d(TAG, "getCommand()");

        checkChannel(channelName);
        return getInstance(instanceId).getCommand();
    }

    @Override
    public String getPackages(UnsignedIntegerFourBytes instanceId, String channelName) throws RenderingControlException {
        Log.d(TAG, "getPackages()");

        checkChannel(channelName);
        return getInstance(instanceId).getPackages();
    }

    @Override
    public void setCommand(UnsignedIntegerFourBytes instanceId, String channelName, String desiredCommand) throws RenderingControlException {
        Log.d(TAG, "setCommand() = " + desiredCommand);

        checkChannel(channelName);
        getInstance(instanceId).setCommand(desiredCommand);
    }

    @Override
    public UnsignedIntegerTwoBytes getVolume(UnsignedIntegerFourBytes instanceId, String channelName) throws RenderingControlException {
        Log.d(TAG, "getVolume()");

        checkChannel(channelName);
        int vol = (int) (getInstance(instanceId).getVolume() * 100);
        return new UnsignedIntegerTwoBytes(vol);
    }

    @Override
    public void setVolume(UnsignedIntegerFourBytes instanceId, String channelName, UnsignedIntegerTwoBytes desiredVolume) throws RenderingControlException {
        checkChannel(channelName);
        Log.d(TAG, "setVolume() = " + desiredVolume);

        double vol = desiredVolume.getValue() / 100d;
        getInstance(instanceId).setVolume(vol);
    }

    @Override
    protected Channel[] getCurrentChannels() {
        Log.d(TAG, "getCurrentChannels()");

        return new Channel[] {
                Channel.Master
        };
    }

    @Override
    public UnsignedIntegerFourBytes[] getCurrentInstanceIds() {
        Log.d(TAG, "getCurrentInstanceIds()");

        UnsignedIntegerFourBytes[] ids = new UnsignedIntegerFourBytes[getPlayers().size()];
        int i = 0;
        for (UnsignedIntegerFourBytes id : getPlayers().keySet()) {
            ids[i] = id;
            i++;
        }
        return ids;
    }
}
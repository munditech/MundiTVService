package tk.munditv.mcontroller.dmr;

import android.util.Log;

import org.fourthline.cling.model.types.ErrorCode;
import org.fourthline.cling.model.types.UnsignedIntegerFourBytes;
import org.fourthline.cling.support.lastchange.LastChange;
import org.fourthline.cling.support.model.Channel;

import java.util.Map;

import tk.munditv.libtvservice.support.tvcontrol.AbstractTVControl;
import tk.munditv.libtvservice.support.tvcontrol.TVControlErrorCode;
import tk.munditv.libtvservice.support.tvcontrol.TVControlException;

public class TVControl extends AbstractTVControl {

    private final static String TAG = TVControl.class.getSimpleName();

    final private Map<UnsignedIntegerFourBytes, MOSMediaPlayer> players;

    protected TVControl(LastChange lastChange, Map<UnsignedIntegerFourBytes, MOSMediaPlayer> players) {
        super(lastChange);
        Log.d(TAG, "AudioRenderingControl()");

        this.players = players;
    }

    protected MOSMediaPlayer getInstance(UnsignedIntegerFourBytes instanceId) throws TVControlException {
        Log.d(TAG, "getInstance()");

        MOSMediaPlayer player = getPlayers().get(instanceId);
        if (player == null) {
            throw new TVControlException(TVControlErrorCode.INVALID_INSTANCE_ID);
        }
        return player;
    }

    protected void checkChannel(String channelName) throws TVControlException {
        Log.d(TAG, "checkChannel()");

        if (!getChannel(channelName).equals(Channel.Master)) {
            throw new TVControlException(ErrorCode.ARGUMENT_VALUE_INVALID, "Unsupported audio channel: " + channelName);
        }
    }

    protected Map<UnsignedIntegerFourBytes, MOSMediaPlayer> getPlayers() {
        Log.d(TAG, "getPlayers()");
        return players;
    }

    @Override
    public String getCommand(UnsignedIntegerFourBytes instanceId, String channelName) throws TVControlException {
        Log.d(TAG, "getCommand()");

        checkChannel(channelName);
        return getInstance(instanceId).getCommand();
    }

    @Override
    public String getPackages(UnsignedIntegerFourBytes instanceId, String channelName) throws TVControlException {
        Log.d(TAG, "getPackages()");

        checkChannel(channelName);
        return getInstance(instanceId).getPackages();
    }

    @Override
    public void setCommand(UnsignedIntegerFourBytes instanceId, String channelName, String desiredCommand) throws TVControlException {
        Log.d(TAG, "setCommand() = " + desiredCommand);

        checkChannel(channelName);
        getInstance(instanceId).setCommand(desiredCommand);
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

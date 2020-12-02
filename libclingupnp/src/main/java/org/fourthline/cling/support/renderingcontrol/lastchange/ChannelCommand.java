package org.fourthline.cling.support.renderingcontrol.lastchange;

import android.util.Log;

import org.fourthline.cling.support.model.Channel;

public class ChannelCommand {

    private final static String TAG = ChannelCommand.class.getSimpleName();

    protected Channel channel;
    protected String command;

    public ChannelCommand(Channel channel, String command) {
        Log.d(TAG, "ChannelCommand()");
        this.channel = channel;
        this.command = command;
    }

    public Channel getChannel() {
        Log.d(TAG, "getChannel()");
        return channel;
    }

    public String getCommand() {
        Log.d(TAG, "getCommand()");

        return command;
    }

    @Override
    public String toString() {
        Log.d(TAG, "toString()");

        return "Command: " + getCommand() + " (" + getChannel() + ")";
    }

}

package org.fourthline.cling.support.renderingcontrol.lastchange;

import android.util.Log;

import org.fourthline.cling.support.model.Channel;

public class ChannelPackages {

    private final static String TAG = ChannelPackages.class.getSimpleName();
    protected Channel channel;
    protected String packages;

    public ChannelPackages(Channel channel, String packages) {
        Log.d(TAG, "ChannelPackages()");

        this.channel = channel;
        this.packages = packages;
    }

    public Channel getChannel() {
        Log.d(TAG, "getChannel()");

        return channel;
    }

    public String getPackages() {
        Log.d(TAG, "getPackages()");

        return packages;
    }

    @Override
    public String toString() {
        Log.d(TAG, "toString()");

        return "getPackages: " + getPackages() + " (" + getChannel() + ")";
    }

}

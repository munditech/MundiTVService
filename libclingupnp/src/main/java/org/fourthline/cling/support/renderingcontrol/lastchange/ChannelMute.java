/*
 * Copyright (C) 2013 4th Line GmbH, Switzerland
 *
 * The contents of this file are subject to the terms of either the GNU
 * Lesser General Public License Version 2 or later ("LGPL") or the
 * Common Development and Distribution License Version 1 or later
 * ("CDDL") (collectively, the "License"). You may not use this file
 * except in compliance with the License. See LICENSE.txt for more
 * information.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 */

package org.fourthline.cling.support.renderingcontrol.lastchange;

import android.util.Log;

import org.fourthline.cling.support.model.Channel;

/**
 * @author Christian Bauer
 */
public class ChannelMute {

    private final static String TAG = ChannelMute.class.getSimpleName();

    protected Channel channel;
    protected Boolean mute;

    public ChannelMute(Channel channel, Boolean mute) {
        Log.d(TAG, "ChannelMute()");

        this.channel = channel;
        this.mute = mute;
    }

    public Channel getChannel() {
        Log.d(TAG, "getChannel()");

        return channel;
    }

    public Boolean getMute() {
        Log.d(TAG, "getMute()");

        return mute;
    }

    @Override
    public String toString() {
        Log.d(TAG, "toString()");

        return "Mute: " + getMute() + " (" + getChannel() + ")";
    }
}

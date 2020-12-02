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

package org.fourthline.cling.support.renderingcontrol.callback;

import android.util.Log;

import org.fourthline.cling.controlpoint.ActionCallback;
import org.fourthline.cling.model.action.ActionInvocation;
import org.fourthline.cling.model.meta.Service;
import org.fourthline.cling.model.types.UnsignedIntegerFourBytes;
import org.fourthline.cling.support.model.Channel;

import java.util.logging.Logger;

/**
 *
 * @author Christian Bauer
 */
public abstract class GetCommand extends ActionCallback {

    private final static String TAG = GetCommand.class.getSimpleName();

    public GetCommand(Service service) {
        this(new UnsignedIntegerFourBytes(0), service);
        Log.d(TAG, "GetCommand()");
    }
    public GetCommand(UnsignedIntegerFourBytes instanceId, Service service) {
        super(new ActionInvocation(service.getAction("GetCommand")));
        Log.d(TAG, "GetCommand()");
        getActionInvocation().setInput("InstanceID", instanceId);
        getActionInvocation().setInput("Channel", Channel.Master.toString());
    }

    public void success(ActionInvocation invocation) {
        Log.d(TAG, "success()");
        String currentCommand = (String) invocation.getOutput("CurrentCommand").getValue();
        received(invocation, currentCommand);
    }

    public abstract void received(ActionInvocation actionInvocation, String currentCommand);
}
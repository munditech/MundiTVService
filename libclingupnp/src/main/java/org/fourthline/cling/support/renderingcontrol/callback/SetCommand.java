package org.fourthline.cling.support.renderingcontrol.callback;

import android.util.Log;

import org.fourthline.cling.controlpoint.ActionCallback;
import org.fourthline.cling.model.action.ActionInvocation;
import org.fourthline.cling.model.meta.Service;
import org.fourthline.cling.model.types.UnsignedIntegerFourBytes;
import org.fourthline.cling.support.model.Channel;
import org.fourthline.cling.support.renderingcontrol.callback.SetMute;

import java.util.logging.Logger;

public abstract  class SetCommand extends ActionCallback {

    private final static String TAG = SetCommand.class.getSimpleName();

    public SetCommand(Service service, String desiredCommand) {
        this(new UnsignedIntegerFourBytes(0), service, desiredCommand);
        Log.d(TAG, "SetCommand()");
    }

    public SetCommand(UnsignedIntegerFourBytes instanceId, Service service, String desiredCommand) {
        super(new ActionInvocation(service.getAction("SetCommand")));
        Log.d(TAG, "SetCommand()");
        getActionInvocation().setInput("InstanceID", instanceId);
        getActionInvocation().setInput("Channel", Channel.Master.toString());
        getActionInvocation().setInput("desiredCommand", desiredCommand);
    }

    @Override
    public void success(ActionInvocation invocation) {
        Log.d(TAG, "success()");
    }
}

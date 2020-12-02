package org.fourthline.cling.support.renderingcontrol.callback;

import android.util.Log;

import org.fourthline.cling.controlpoint.ActionCallback;
import org.fourthline.cling.model.action.ActionInvocation;
import org.fourthline.cling.model.meta.Service;
import org.fourthline.cling.model.types.UnsignedIntegerFourBytes;
import org.fourthline.cling.support.model.Channel;

public abstract class GetPackages extends ActionCallback  {

    private final static String TAG = GetPackages.class.getSimpleName();

    public GetPackages(Service service) {
        this(new UnsignedIntegerFourBytes(0), service);
        Log.d(TAG, "GetPackages() constructor");
    }
    public GetPackages(UnsignedIntegerFourBytes instanceId, Service service) {
        super(new ActionInvocation(service.getAction("GetPackages")));
        Log.d(TAG, "GetPackages()");

        getActionInvocation().setInput("InstanceID", instanceId);
        getActionInvocation().setInput("Channel", Channel.Master.toString());
    }

    public void success(ActionInvocation invocation) {
        Log.d(TAG, "success()!");

        String currentPackages = (String) invocation.getOutput("CurrentPackages").getValue();
        //Log.d(TAG, "currentPackages = " + currentPackages);

        received(invocation, currentPackages);
    }

    public abstract void received(ActionInvocation actionInvocation, String currentPackages);
}

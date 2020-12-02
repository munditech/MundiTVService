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

package tk.munditv.libtvservice.support.tvcontrol;

import org.fourthline.cling.binding.annotations.UpnpAction;
import org.fourthline.cling.binding.annotations.UpnpInputArgument;
import org.fourthline.cling.binding.annotations.UpnpOutputArgument;
import org.fourthline.cling.binding.annotations.UpnpService;
import org.fourthline.cling.binding.annotations.UpnpServiceId;
import org.fourthline.cling.binding.annotations.UpnpServiceType;
import org.fourthline.cling.binding.annotations.UpnpStateVariable;
import org.fourthline.cling.binding.annotations.UpnpStateVariables;
import org.fourthline.cling.model.types.ErrorCode;
import org.fourthline.cling.model.types.UnsignedIntegerFourBytes;
import org.fourthline.cling.support.lastchange.LastChange;
import org.fourthline.cling.support.lastchange.LastChangeDelegator;
import org.fourthline.cling.support.model.Channel;
import org.fourthline.cling.support.model.PresetName;

import java.beans.PropertyChangeSupport;

import tk.munditv.libtvservice.support.tvcontrol.lastchange.ChannelCommand;
import tk.munditv.libtvservice.support.tvcontrol.lastchange.ChannelPackages;
import tk.munditv.libtvservice.support.tvcontrol.lastchange.TVControlLastChangeParser;
import tk.munditv.libtvservice.support.tvcontrol.lastchange.TVControlVariable;

/**
 *
 */
@UpnpService(
        serviceId = @UpnpServiceId("TVControl"),
        serviceType = @UpnpServiceType(value = "TVControl", version = 1),
        stringConvertibleTypes = LastChange.class
)
@UpnpStateVariables({
        @UpnpStateVariable(
                name = "PresetNameList",
                sendEvents = false,
                datatype = "string"),
        @UpnpStateVariable(
                name = "Command",
                sendEvents = false,
                datatype = "string"),
        @UpnpStateVariable(
                name = "Packages",
                sendEvents = false,
                datatype = "string"),
        @UpnpStateVariable(
                name = "A_ARG_TYPE_Channel",
                sendEvents = false,
                allowedValuesEnum = Channel.class),
        @UpnpStateVariable(
                name = "A_ARG_TYPE_PresetName",
                sendEvents = false,
                allowedValuesEnum = PresetName.class),
        @UpnpStateVariable(
                name = "A_ARG_TYPE_InstanceID",
                sendEvents = false,
                datatype = "ui4")

})
public abstract class AbstractTVControl implements LastChangeDelegator {

    private final static String TAG = AbstractTVControl.class.getSimpleName();

    @UpnpStateVariable(eventMaximumRateMilliseconds = 200)
    final private LastChange lastChange;

    final protected PropertyChangeSupport propertyChangeSupport;

    protected AbstractTVControl() {
        this.propertyChangeSupport = new PropertyChangeSupport(this);
        this.lastChange = new LastChange(new TVControlLastChangeParser());
    }

    protected AbstractTVControl(LastChange lastChange) {
        this.propertyChangeSupport = new PropertyChangeSupport(this);
        this.lastChange = lastChange;
    }

    protected AbstractTVControl(PropertyChangeSupport propertyChangeSupport) {
        this.propertyChangeSupport = propertyChangeSupport;
        this.lastChange = new LastChange(new TVControlLastChangeParser());
    }

    protected AbstractTVControl(PropertyChangeSupport propertyChangeSupport, LastChange lastChange) {
        this.propertyChangeSupport = propertyChangeSupport;
        this.lastChange = lastChange;
    }

    @Override
    public LastChange getLastChange() {
        return lastChange;
    }

    @Override
    public void appendCurrentState(LastChange lc, UnsignedIntegerFourBytes instanceId) throws Exception {
        for (Channel channel : getCurrentChannels()) {
            String channelString = channel.name();
            lc.setEventedValue(
                    instanceId,
                    new TVControlVariable.Command(new ChannelCommand(channel, getCommand(instanceId, channelString))),
                    new TVControlVariable.Packages(new ChannelPackages(channel, getPackages(instanceId, channelString))),
                    new TVControlVariable.PresetNameList(PresetName.FactoryDefaults.name())
            );
        }
    }

    public PropertyChangeSupport getPropertyChangeSupport() {
        return propertyChangeSupport;
    }

    public static UnsignedIntegerFourBytes getDefaultInstanceID() {
        return new UnsignedIntegerFourBytes(0);
    }

    @UpnpAction(out = @UpnpOutputArgument(name = "CurrentPresetNameList", stateVariable = "PresetNameList"))
    public String listPresets(@UpnpInputArgument(name = "InstanceID") UnsignedIntegerFourBytes instanceId) throws TVControlException {
        return PresetName.FactoryDefaults.toString();
    }

    @UpnpAction
    public void selectPreset(@UpnpInputArgument(name = "InstanceID") UnsignedIntegerFourBytes instanceId,
                             @UpnpInputArgument(name = "PresetName") String presetName) throws TVControlException {
    }

    @UpnpAction(out = @UpnpOutputArgument(name = "CurrentCommand", stateVariable = "Command"))
    public abstract String getCommand(@UpnpInputArgument(name = "InstanceID") UnsignedIntegerFourBytes instanceId,
                                    @UpnpInputArgument(name = "Channel") String channelName) throws TVControlException;

    @UpnpAction(out = @UpnpOutputArgument(name = "CurrentPackages", stateVariable = "Packages"))
    public abstract String getPackages(@UpnpInputArgument(name = "InstanceID") UnsignedIntegerFourBytes instanceId,
                                      @UpnpInputArgument(name = "Channel") String channelName) throws TVControlException;

    @UpnpAction
    public abstract void setCommand(@UpnpInputArgument(name = "InstanceID") UnsignedIntegerFourBytes instanceId,
                                 @UpnpInputArgument(name = "Channel") String channelName,
                                 @UpnpInputArgument(name = "DesiredCommand", stateVariable = "Command") String desiredCommand) throws TVControlException;

    protected abstract Channel[] getCurrentChannels();

    protected Channel getChannel(String channelName) throws TVControlException {
        try {
            return Channel.valueOf(channelName);
        } catch (IllegalArgumentException ex) {
            throw new TVControlException(ErrorCode.ARGUMENT_VALUE_INVALID, "Unsupported audio channel: " + channelName);
        }
    }

}

package tk.munditv.libtvservice.support.tvcontrol.lastchange;

import android.util.Log;

import org.fourthline.cling.model.types.Datatype;
import org.fourthline.cling.model.types.InvalidValueException;
import org.fourthline.cling.model.types.StringDatatype;
import org.fourthline.cling.support.lastchange.EventedValue;
import org.fourthline.cling.support.model.Channel;
import org.fourthline.cling.support.shared.AbstractMap;

import java.util.Map;

public class EventedValueChannelPackages extends EventedValue<ChannelPackages> {

    private final static String TAG = EventedValueChannelPackages.class.getSimpleName();

    public EventedValueChannelPackages(ChannelPackages value) {
        super(value);
        Log.d(TAG, "EventedValueChannelPackages()");
    }

    public EventedValueChannelPackages(Map.Entry<String, String>[] attributes) {
        super(attributes);
        Log.d(TAG, "EventedValueChannelPackages()");
    }

    @Override
    protected ChannelPackages valueOf(Map.Entry<String, String>[] attributes) throws InvalidValueException {
        Log.d(TAG, "valueOf()");

        Channel channel = null;
        String packages = null;
        for (Map.Entry<String, String> attribute : attributes) {
            if (attribute.getKey().equals("channel"))
                channel = Channel.valueOf(attribute.getValue());
            if (attribute.getKey().equals("val"))
                packages = new StringDatatype().valueOf(attribute.getValue());
        }
        return channel != null && packages != null ? new ChannelPackages(channel, packages) : null;
    }

    @Override
    public Map.Entry<String, String>[] getAttributes() {
        Log.d(TAG, "getAttributes()");

        return new Map.Entry[]{
                new AbstractMap.SimpleEntry<>(
                        "val",
                        new StringDatatype().getString(getValue().getPackages())
                ),
                new AbstractMap.SimpleEntry<>(
                        "channel",
                        getValue().getChannel().name()
                )
        };
    }

    @Override
    public String toString() {
        Log.d(TAG, "toString()");

        return getValue().toString();
    }

    @Override
    protected Datatype getDatatype() {
        Log.d(TAG, "getDatatype()");

        return null; // Not needed
    }
}

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

package tk.munditv.libtvservice.support.tvcontrol.lastchange;

import android.util.Log;

import org.fourthline.cling.support.lastchange.EventedValue;
import org.fourthline.cling.support.lastchange.EventedValueString;
import org.fourthline.cling.support.renderingcontrol.lastchange.RenderingControlVariable;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * @author Christian Bauer
 */
public class TVControlVariable {

    private final static String TAG = TVControlVariable.class.getSimpleName();

    public static Set<Class<? extends EventedValue>> ALL = new HashSet<Class<? extends EventedValue>>() {{
        add(PresetNameList.class);
        add(Command.class);
        add(Packages.class);
    }};

    public static class PresetNameList extends EventedValueString {
        public PresetNameList(String s) {
            super(s);
            Log.d(TAG, "PresetNameList -- PresetNameList()");
        }

        public PresetNameList(Map.Entry<String, String>[] attributes) {
            super(attributes);
        }
    }

    public static class Command extends EventedValueChannelCommand {
        public Command(ChannelCommand value) {
            super(value);
        }

        public Command(Map.Entry<String, String>[] attributes) {
            super(attributes);
        }
    }

    public static class Packages extends EventedValueChannelPackages {
        public Packages(ChannelPackages value) {
            super(value);
        }

        public Packages(Map.Entry<String, String>[] attributes) {
            super(attributes);
        }
    }

}

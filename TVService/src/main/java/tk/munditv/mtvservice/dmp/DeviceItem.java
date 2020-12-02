/*
 * Copyright (C) 2010 Teleal GmbH, Switzerland
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3 of
 * the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package tk.munditv.mtvservice.dmp;

import android.graphics.drawable.Drawable;
import android.util.Log;

import org.fourthline.cling.model.meta.Device;
import org.fourthline.cling.model.types.UDN;

/**
 * Wraps a <tt>Device</tt> for display with icon and label. Equality is
 * implemented with UDN comparison.
 */
public class DeviceItem {

    private final static String TAG = DeviceItem.class.getSimpleName();

    private UDN udn;
    private Device device;
    // TODO do we need label ?
    private String[] label;
    private Drawable icon;

    public DeviceItem(Device device) {
        Log.d(TAG, "DeviceItem()");

        this.udn = device.getIdentity().getUdn();
        this.device = device;
    }

    public DeviceItem(Device device, String... label) {
        Log.d(TAG, "DeviceItem()");
        this.udn = device.getIdentity().getUdn();
        this.device = device;
        this.label = label;
    }

    public DeviceItem() {
        Log.d(TAG, "DeviceItem()");
    }

    public UDN getUdn() {
        Log.d(TAG, "getUdn()");
        return udn;
    }

    public Device getDevice() {
        Log.d(TAG, "getDevice()");

        return device;
    }

    public String[] getLabel() {
        Log.d(TAG, "getLabel()");

        return label;
    }

    public void setLabel(String[] label) {
        Log.d(TAG, "setLabel()");

        this.label = label;
    }

    public Drawable getIcon() {
        Log.d(TAG, "getIcon()");

        return icon;
    }

    public void setIcon(Drawable icon) {
        Log.d(TAG, "setIcon()");

        this.icon = icon;
    }

    @Override
    public boolean equals(Object o) {
        Log.d(TAG, "equals()");

        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        DeviceItem that = (DeviceItem) o;

        if (!udn.equals(that.udn))
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        Log.d(TAG, "hashCode()");

        return udn.hashCode();
    }

    @Override
    public String toString() {
        Log.d(TAG, "toString()");

        String display;

        if (device.getDetails().getFriendlyName() != null)
            display = device.getDetails().getFriendlyName();
        else
            display = device.getDisplayString();

        // Display a little star while the device is being loaded (see
        // performance optimization earlier)
        return device.isFullyHydrated() ? display : display + " *";
    }
}

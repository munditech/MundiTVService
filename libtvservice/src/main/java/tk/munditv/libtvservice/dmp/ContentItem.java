package tk.munditv.libtvservice.dmp;

import android.util.Log;

import org.fourthline.cling.model.meta.Device;
import org.fourthline.cling.model.meta.Service;
import org.fourthline.cling.support.model.DIDLObject;
import org.fourthline.cling.support.model.container.Container;
import org.fourthline.cling.support.model.item.Item;

public class ContentItem {

	private final static String TAG = ContentItem.class.getSimpleName();

	private Device device;
	private Service service;
	private DIDLObject content;
	private String id;
	private Boolean isContainer;

	public ContentItem(Container container, Service service, Device device) {
		Log.d(TAG,"ContentItem()");

		// TODO Auto-generated constructor stub
		this.device = device;
		this.service = service;
		this.content = container;
		this.id = container.getId();
		this.isContainer = true;
	}

	public ContentItem(Container container, Service service) {
		Log.d(TAG,"ContentItem()");

		// TODO Auto-generated constructor stub
		this.service = service;
		this.content = container;
		this.id = container.getId();
		this.isContainer = true;
	}

	public ContentItem(Item item, Service service) {
		Log.d(TAG,"ContentItem()");

		// TODO Auto-generated constructor stub
		this.service = service;
		this.content = item;
		this.id = item.getId();
		this.isContainer = false;
	}

	public Container getContainer() {
		Log.d(TAG,"getContainer()");

		if (isContainer)
			return (Container) content;
		else {
			return null;
		}
	}

	public Item getItem() {
		Log.d(TAG,"getItem()");

		if (isContainer)
			return null;
		else
			return (Item) content;
	}

	public Service getService() {
		Log.d(TAG,"getService()");

		return service;
	}

	public Device getDevice() {
		Log.d(TAG,"getDevice()");

		return device;
	}

	public Boolean isContainer() {
		Log.d(TAG,"isContainer()");

		return isContainer;
	}

	@Override
	public boolean equals(Object o) {
		Log.d(TAG,"equals()");

		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;

		ContentItem that = (ContentItem) o;

		if (!id.equals(that.id))
			return false;

		return true;
	}

	@Override
	public int hashCode() {
		Log.d(TAG,"hashCode()");

		return content.hashCode();
	}

	@Override
	public String toString() {
		Log.d(TAG,"toString()");

		return content.getTitle();
	}
}

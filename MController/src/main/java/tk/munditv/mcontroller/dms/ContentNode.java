package tk.munditv.mcontroller.dms;

import android.util.Log;

import org.fourthline.cling.support.model.container.Container;
import org.fourthline.cling.support.model.item.Item;

public class ContentNode {

		private final static String TAG = ContentNode.class.getSimpleName();

		private Container container;
		private Item item;
		private String id;
		private String fullPath;
		private boolean isItem;
		
		public ContentNode(String id, Container container) {
			Log.d(TAG, "ContentNode()");

			this.id = id;
			this.container = container;
			this.fullPath = null;
			this.isItem = false;
		}
		
		public ContentNode(String id, Item item, String fullPath) {
			Log.d(TAG, "ContentNode()");

			this.id = id;
			this.item = item;
			this.fullPath = fullPath;
			this.isItem = true;
		}
		
		public String getId() {
			Log.d(TAG, "getId()");

			return id;
		}
		
		public Container getContainer() {
			Log.d(TAG, "getContainer()");

			return container;
		}
		
		public Item getItem() {
			Log.d(TAG, "getItem()");

			return item;
		}
		
		public String getFullPath() {
			Log.d(TAG, "getFullPath()");

			if (isItem && fullPath != null) {
				return fullPath;
			}
			return null;
		}
		
		public boolean isItem() {
			Log.d(TAG, "isItem()");

			return isItem;
		}
}

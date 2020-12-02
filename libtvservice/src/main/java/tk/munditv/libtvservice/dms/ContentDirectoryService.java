package tk.munditv.libtvservice.dms;

import android.util.Log;

import org.fourthline.cling.support.contentdirectory.AbstractContentDirectoryService;
import org.fourthline.cling.support.contentdirectory.ContentDirectoryErrorCode;
import org.fourthline.cling.support.contentdirectory.ContentDirectoryException;
import org.fourthline.cling.support.contentdirectory.DIDLParser;
import org.fourthline.cling.support.model.BrowseFlag;
import org.fourthline.cling.support.model.BrowseResult;
import org.fourthline.cling.support.model.DIDLContent;
import org.fourthline.cling.support.model.SortCriterion;
import org.fourthline.cling.support.model.container.Container;
import org.fourthline.cling.support.model.item.Item;

public class ContentDirectoryService extends AbstractContentDirectoryService {

	private final static String TAG = ContentDirectoryService.class.getCanonicalName();

	@Override
	public BrowseResult browse(String objectID, BrowseFlag browseFlag,
                               String filter, long firstResult, long maxResults,
                               SortCriterion[] orderby) throws ContentDirectoryException {
		Log.d(TAG, "browse()");

		// TODO Auto-generated method stub
		try {

			DIDLContent didl = new DIDLContent();

			ContentNode contentNode = ContentTree.getNode(objectID);
			
			Log.v(TAG, "someone's browsing id: " + objectID);

			if (contentNode == null)
				return new BrowseResult("", 0, 0);

			if (contentNode.isItem()) {
				didl.addItem(contentNode.getItem());
				
				Log.v(TAG, "returing item: " + contentNode.getItem().getTitle());
				
				return new BrowseResult(new DIDLParser().generate(didl), 1, 1);
			} else {
				if (browseFlag == BrowseFlag.METADATA) {
					didl.addContainer(contentNode.getContainer());
					
					Log.v(TAG, "returning metadata of container: " + contentNode.getContainer().getTitle());
					
					return new BrowseResult(new DIDLParser().generate(didl), 1,
							1);
				} else {
					for (Container container : contentNode.getContainer()
							.getContainers()) {
						didl.addContainer(container);
						
						Log.v(TAG, "getting child container: " + container.getTitle());
					}
					for (Item item : contentNode.getContainer().getItems()) {
						didl.addItem(item);
						
						Log.v(TAG, "getting child item: " + item.getTitle());
					}
					return new BrowseResult(new DIDLParser().generate(didl),
							contentNode.getContainer().getChildCount(),
							contentNode.getContainer().getChildCount());
				}

			}

		} catch (Exception ex) {
			throw new ContentDirectoryException(
					ContentDirectoryErrorCode.CANNOT_PROCESS, ex.toString());
		}
	}

	@Override
	public BrowseResult search(String containerId, String searchCriteria,
                               String filter, long firstResult, long maxResults,
                               SortCriterion[] orderBy) throws ContentDirectoryException {
		Log.d(TAG, "search()");

		// You can override this method to implement searching!
		return super.search(containerId, searchCriteria, filter, firstResult,
				maxResults, orderBy);
	}
}
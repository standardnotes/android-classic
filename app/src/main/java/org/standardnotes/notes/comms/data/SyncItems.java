
package org.standardnotes.notes.comms.data;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Generated;


/**
 * 
 * <p>
 * 
 * 
 */
@Generated("org.jsonschema2pojo")
public class SyncItems {

    @SerializedName("sync_token")
    @Expose
    private String syncToken;
    @SerializedName("retrieved_items")
    @Expose
    private List<EncryptedItem> retrievedItems = new ArrayList<EncryptedItem>();
    @SerializedName("saved_items")
    @Expose
    private List<EncryptedItem> savedItems = new ArrayList<EncryptedItem>();

    /**
     * 
     * @return
     *     The syncToken
     */
    public String getSyncToken() {
        return syncToken;
    }

    /**
     * 
     * @param syncToken
     *     The sync_token
     */
    public void setSyncToken(String syncToken) {
        this.syncToken = syncToken;
    }

    /**
     * 
     * @return
     *     The retrievedItems
     */
    public List<EncryptedItem> getRetrievedItems() {
        return retrievedItems;
    }

    /**
     * 
     * @param retrievedItems
     *     The retrieved_items
     */
    public void setRetrievedItems(List<EncryptedItem> retrievedItems) {
        this.retrievedItems = retrievedItems;
    }

    /**
     * 
     * @return
     *     The savedItems
     */
    public List<EncryptedItem> getSavedItems() {
        return savedItems;
    }

    /**
     * 
     * @param savedItems
     *     The saved_items
     */
    public void setSavedItems(List<EncryptedItem> savedItems) {
        this.savedItems = savedItems;
    }

}

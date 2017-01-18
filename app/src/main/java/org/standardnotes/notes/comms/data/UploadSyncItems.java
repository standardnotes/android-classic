
package org.standardnotes.notes.comms.data;

import java.util.ArrayList;
import java.util.List;
import javax.annotation.Generated;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;


/**
 * 
 * <p>
 * 
 * 
 */
@Generated("org.jsonschema2pojo")
public class UploadSyncItems {

    @SerializedName("sync_token")
    @Expose
    private String syncToken;
    @SerializedName("items")
    @Expose
    private List<EncryptedItem> items = new ArrayList<EncryptedItem>();

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
     *     The items
     */
    public List<EncryptedItem> getItems() {
        return items;
    }

    /**
     * 
     * @param items
     *     The items
     */
    public void setItems(List<EncryptedItem> items) {
        this.items = items;
    }

}

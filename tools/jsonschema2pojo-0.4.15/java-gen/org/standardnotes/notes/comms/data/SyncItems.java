
package org.standardnotes.notes.comms.data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import javax.annotation.Generated;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * 
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@Generated("org.jsonschema2pojo")
@JsonPropertyOrder({
    "sync_token",
    "retrieved_items",
    "saved_items",
    "unsaved"
})
public class SyncItems {

    @JsonProperty("sync_token")
    private String syncToken;
    @JsonProperty("retrieved_items")
    private List<EncryptedItem> retrievedItems = new ArrayList<EncryptedItem>();
    @JsonProperty("saved_items")
    private List<EncryptedItem> savedItems = new ArrayList<EncryptedItem>();
    @JsonProperty("unsaved")
    private List<HashMap> unsaved = new ArrayList<HashMap>();

    /**
     * 
     * @return
     *     The syncToken
     */
    @JsonProperty("sync_token")
    public String getSyncToken() {
        return syncToken;
    }

    /**
     * 
     * @param syncToken
     *     The sync_token
     */
    @JsonProperty("sync_token")
    public void setSyncToken(String syncToken) {
        this.syncToken = syncToken;
    }

    /**
     * 
     * @return
     *     The retrievedItems
     */
    @JsonProperty("retrieved_items")
    public List<EncryptedItem> getRetrievedItems() {
        return retrievedItems;
    }

    /**
     * 
     * @param retrievedItems
     *     The retrieved_items
     */
    @JsonProperty("retrieved_items")
    public void setRetrievedItems(List<EncryptedItem> retrievedItems) {
        this.retrievedItems = retrievedItems;
    }

    /**
     * 
     * @return
     *     The savedItems
     */
    @JsonProperty("saved_items")
    public List<EncryptedItem> getSavedItems() {
        return savedItems;
    }

    /**
     * 
     * @param savedItems
     *     The saved_items
     */
    @JsonProperty("saved_items")
    public void setSavedItems(List<EncryptedItem> savedItems) {
        this.savedItems = savedItems;
    }

    /**
     * 
     * @return
     *     The unsaved
     */
    @JsonProperty("unsaved")
    public List<HashMap> getUnsaved() {
        return unsaved;
    }

    /**
     * 
     * @param unsaved
     *     The unsaved
     */
    @JsonProperty("unsaved")
    public void setUnsaved(List<HashMap> unsaved) {
        this.unsaved = unsaved;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(syncToken).append(retrievedItems).append(savedItems).append(unsaved).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof SyncItems) == false) {
            return false;
        }
        SyncItems rhs = ((SyncItems) other);
        return new EqualsBuilder().append(syncToken, rhs.syncToken).append(retrievedItems, rhs.retrievedItems).append(savedItems, rhs.savedItems).append(unsaved, rhs.unsaved).isEquals();
    }

}


package org.standardnotes.notes.comms.data;

import java.util.ArrayList;
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
    "items"
})
public class UploadSyncItems {

    @JsonProperty("sync_token")
    private String syncToken;
    @JsonProperty("items")
    private List<EncryptedItem> items = new ArrayList<EncryptedItem>();

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
     *     The items
     */
    @JsonProperty("items")
    public List<EncryptedItem> getItems() {
        return items;
    }

    /**
     * 
     * @param items
     *     The items
     */
    @JsonProperty("items")
    public void setItems(List<EncryptedItem> items) {
        this.items = items;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(syncToken).append(items).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof UploadSyncItems) == false) {
            return false;
        }
        UploadSyncItems rhs = ((UploadSyncItems) other);
        return new EqualsBuilder().append(syncToken, rhs.syncToken).append(items, rhs.items).isEquals();
    }

}

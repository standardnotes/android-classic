
package org.standardnotes.notes.comms.data;

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
public class UnsavedItem {

    /**
     * 
     * <p>
     * 
     * 
     */
    @SerializedName("item")
    @Expose
    private EncryptedItem item;
    /**
     * 
     * <p>
     * 
     * 
     */
    @SerializedName("error")
    @Expose
    private UnsavedItemError error;

    /**
     * 
     * <p>
     * 
     * 
     * @return
     *     The item
     */
    public EncryptedItem getItem() {
        return item;
    }

    /**
     * 
     * <p>
     * 
     * 
     * @param item
     *     The item
     */
    public void setItem(EncryptedItem item) {
        this.item = item;
    }

    /**
     * 
     * <p>
     * 
     * 
     * @return
     *     The error
     */
    public UnsavedItemError getError() {
        return error;
    }

    /**
     * 
     * <p>
     * 
     * 
     * @param error
     *     The error
     */
    public void setError(UnsavedItemError error) {
        this.error = error;
    }

}

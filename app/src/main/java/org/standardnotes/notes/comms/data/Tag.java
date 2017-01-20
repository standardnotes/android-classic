
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
public class Tag
    extends EncryptableItem
{

    @SerializedName("title")
    @Expose
    private String title;
    @SerializedName("dirty")
    @Expose
    private Boolean dirty;

    /**
     * 
     * @return
     *     The title
     */
    public String getTitle() {
        return title;
    }

    /**
     * 
     * @param title
     *     The title
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * 
     * @return
     *     The dirty
     */
    public Boolean getDirty() {
        return dirty;
    }

    /**
     * 
     * @param dirty
     *     The dirty
     */
    public void setDirty(Boolean dirty) {
        this.dirty = dirty;
    }

}

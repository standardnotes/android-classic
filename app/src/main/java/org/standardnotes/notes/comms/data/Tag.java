
package org.standardnotes.notes.comms.data;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import javax.annotation.Generated;


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
    private String title = "";

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

}

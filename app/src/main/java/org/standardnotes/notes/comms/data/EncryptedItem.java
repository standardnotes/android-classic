
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
public class EncryptedItem
    extends EncryptableItem
{

    @SerializedName("content")
    @Expose
    private String content;
    @SerializedName("content_type")
    @Expose
    private String contentType;
    @SerializedName("auth_hash")
    @Expose
    private String authHash;

    /**
     * 
     * @return
     *     The content
     */
    public String getContent() {
        return content;
    }

    /**
     * 
     * @param content
     *     The content
     */
    public void setContent(String content) {
        this.content = content;
    }

    /**
     * 
     * @return
     *     The contentType
     */
    public String getContentType() {
        return contentType;
    }

    /**
     * 
     * @param contentType
     *     The content_type
     */
    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    /**
     * 
     * @return
     *     The authHash
     */
    public String getAuthHash() {
        return authHash;
    }

    /**
     * 
     * @param authHash
     *     The auth_hash
     */
    public void setAuthHash(String authHash) {
        this.authHash = authHash;
    }

}

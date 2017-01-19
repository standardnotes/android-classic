
package org.standardnotes.notes.comms.data;

import javax.annotation.Generated;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import org.joda.time.DateTime;


/**
 * 
 * <p>
 * 
 * 
 */
@Generated("org.jsonschema2pojo")
public class EncryptedItem {

    @SerializedName("uuid")
    @Expose
    private String uuid;
    @SerializedName("content")
    @Expose
    private String content;
    @SerializedName("content_type")
    @Expose
    private String contentType;
    @SerializedName("created_at")
    @Expose
    private DateTime createdAt;
    @SerializedName("updated_at")
    @Expose
    private DateTime updatedAt;
    @SerializedName("enc_item_key")
    @Expose
    private String encItemKey;
    @SerializedName("auth_hash")
    @Expose
    private String authHash;
    @SerializedName("presentation_name")
    @Expose
    private String presentationName = null;
    @SerializedName("deleted")
    @Expose
    private Boolean deleted;

    /**
     * 
     * @return
     *     The uuid
     */
    public String getUuid() {
        return uuid;
    }

    /**
     * 
     * @param uuid
     *     The uuid
     */
    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

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
     *     The createdAt
     */
    public DateTime getCreatedAt() {
        return createdAt;
    }

    /**
     * 
     * @param createdAt
     *     The created_at
     */
    public void setCreatedAt(DateTime createdAt) {
        this.createdAt = createdAt;
    }

    /**
     * 
     * @return
     *     The updatedAt
     */
    public DateTime getUpdatedAt() {
        return updatedAt;
    }

    /**
     * 
     * @param updatedAt
     *     The updated_at
     */
    public void setUpdatedAt(DateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    /**
     * 
     * @return
     *     The encItemKey
     */
    public String getEncItemKey() {
        return encItemKey;
    }

    /**
     * 
     * @param encItemKey
     *     The enc_item_key
     */
    public void setEncItemKey(String encItemKey) {
        this.encItemKey = encItemKey;
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

    /**
     * 
     * @return
     *     The presentationName
     */
    public String getPresentationName() {
        return presentationName;
    }

    /**
     * 
     * @param presentationName
     *     The presentation_name
     */
    public void setPresentationName(String presentationName) {
        this.presentationName = presentationName;
    }

    /**
     * 
     * @return
     *     The deleted
     */
    public Boolean getDeleted() {
        return deleted;
    }

    /**
     * 
     * @param deleted
     *     The deleted
     */
    public void setDeleted(Boolean deleted) {
        this.deleted = deleted;
    }

}


package org.standardnotes.notes.comms.data;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import org.joda.time.DateTime;

import java.util.List;

import javax.annotation.Generated;


/**
 * 
 * <p>
 * 
 * 
 */
@Generated("org.jsonschema2pojo")
public class EncryptableItem {

    @SerializedName("uuid")
    @Expose
    private String uuid;
    @SerializedName("created_at")
    @Expose
    private DateTime createdAt;
    @SerializedName("updated_at")
    @Expose
    private DateTime updatedAt;
    @SerializedName("enc_item_key")
    @Expose
    private String encItemKey;

    @SerializedName("deleted")
    @Expose
    private Boolean deleted = false;
    @SerializedName("dirty")
    @Expose
    private Boolean dirty = false;
    @SerializedName("references")
    @Expose
    private List<Reference> references = null;

    @SerializedName("error_decrypting")
    @Expose
    private Boolean errorDecrypting = false;


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
     * @param errorDecrypting
     */
    public void setErrorDecrypting(Boolean errorDecrypting) { this.errorDecrypting = errorDecrypting; }

    /**
     *
     * @return
     *     The errorDecrypting
     */
    public Boolean getErrorDecrypting() {
        return errorDecrypting;
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

    /**
     * 
     * @return
     *     The references
     */
    public List<Reference> getReferences() {
        return references;
    }

    /**
     * 
     * @param references
     *     The references
     */
    public void setReferences(List<Reference> references) {
        this.references = references;
    }

}

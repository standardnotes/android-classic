
package org.standardnotes.notes.comms.data;

import java.util.Date;
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
    "uuid",
    "created_at",
    "updated_at",
    "enc_item_key",
    "presentation_name",
    "deleted",
    "dirty",
    "references"
})
public class EncryptableItem {

    @JsonProperty("uuid")
    private String uuid;
    @JsonProperty("created_at")
    private Date createdAt;
    @JsonProperty("updated_at")
    private Date updatedAt;
    @JsonProperty("enc_item_key")
    private String encItemKey;
    @JsonProperty("presentation_name")
    private String presentationName = null;
    @JsonProperty("deleted")
    private Boolean deleted = false;
    @JsonProperty("dirty")
    private Boolean dirty = false;
    @JsonProperty("references")
    private List<Reference> references = null;

    /**
     * 
     * @return
     *     The uuid
     */
    @JsonProperty("uuid")
    public String getUuid() {
        return uuid;
    }

    /**
     * 
     * @param uuid
     *     The uuid
     */
    @JsonProperty("uuid")
    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    /**
     * 
     * @return
     *     The createdAt
     */
    @JsonProperty("created_at")
    public Date getCreatedAt() {
        return createdAt;
    }

    /**
     * 
     * @param createdAt
     *     The created_at
     */
    @JsonProperty("created_at")
    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    /**
     * 
     * @return
     *     The updatedAt
     */
    @JsonProperty("updated_at")
    public Date getUpdatedAt() {
        return updatedAt;
    }

    /**
     * 
     * @param updatedAt
     *     The updated_at
     */
    @JsonProperty("updated_at")
    public void setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
    }

    /**
     * 
     * @return
     *     The encItemKey
     */
    @JsonProperty("enc_item_key")
    public String getEncItemKey() {
        return encItemKey;
    }

    /**
     * 
     * @param encItemKey
     *     The enc_item_key
     */
    @JsonProperty("enc_item_key")
    public void setEncItemKey(String encItemKey) {
        this.encItemKey = encItemKey;
    }

    /**
     * 
     * @return
     *     The presentationName
     */
    @JsonProperty("presentation_name")
    public String getPresentationName() {
        return presentationName;
    }

    /**
     * 
     * @param presentationName
     *     The presentation_name
     */
    @JsonProperty("presentation_name")
    public void setPresentationName(String presentationName) {
        this.presentationName = presentationName;
    }

    /**
     * 
     * @return
     *     The deleted
     */
    @JsonProperty("deleted")
    public Boolean getDeleted() {
        return deleted;
    }

    /**
     * 
     * @param deleted
     *     The deleted
     */
    @JsonProperty("deleted")
    public void setDeleted(Boolean deleted) {
        this.deleted = deleted;
    }

    /**
     * 
     * @return
     *     The dirty
     */
    @JsonProperty("dirty")
    public Boolean getDirty() {
        return dirty;
    }

    /**
     * 
     * @param dirty
     *     The dirty
     */
    @JsonProperty("dirty")
    public void setDirty(Boolean dirty) {
        this.dirty = dirty;
    }

    /**
     * 
     * @return
     *     The references
     */
    @JsonProperty("references")
    public List<Reference> getReferences() {
        return references;
    }

    /**
     * 
     * @param references
     *     The references
     */
    @JsonProperty("references")
    public void setReferences(List<Reference> references) {
        this.references = references;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(uuid).append(createdAt).append(updatedAt).append(encItemKey).append(presentationName).append(deleted).append(dirty).append(references).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof EncryptableItem) == false) {
            return false;
        }
        EncryptableItem rhs = ((EncryptableItem) other);
        return new EqualsBuilder().append(uuid, rhs.uuid).append(createdAt, rhs.createdAt).append(updatedAt, rhs.updatedAt).append(encItemKey, rhs.encItemKey).append(presentationName, rhs.presentationName).append(deleted, rhs.deleted).append(dirty, rhs.dirty).append(references, rhs.references).isEquals();
    }

}

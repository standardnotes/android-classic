
package org.standardnotes.notes.comms.data;

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
    "content",
    "content_type",
    "auth_hash"
})
public class EncryptedItem
    extends EncryptableItem
{

    @JsonProperty("content")
    private String content;
    @JsonProperty("content_type")
    private String contentType;
    @JsonProperty("auth_hash")
    private String authHash;

    /**
     * 
     * @return
     *     The content
     */
    @JsonProperty("content")
    public String getContent() {
        return content;
    }

    /**
     * 
     * @param content
     *     The content
     */
    @JsonProperty("content")
    public void setContent(String content) {
        this.content = content;
    }

    /**
     * 
     * @return
     *     The contentType
     */
    @JsonProperty("content_type")
    public String getContentType() {
        return contentType;
    }

    /**
     * 
     * @param contentType
     *     The content_type
     */
    @JsonProperty("content_type")
    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    /**
     * 
     * @return
     *     The authHash
     */
    @JsonProperty("auth_hash")
    public String getAuthHash() {
        return authHash;
    }

    /**
     * 
     * @param authHash
     *     The auth_hash
     */
    @JsonProperty("auth_hash")
    public void setAuthHash(String authHash) {
        this.authHash = authHash;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().appendSuper(super.hashCode()).append(content).append(contentType).append(authHash).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof EncryptedItem) == false) {
            return false;
        }
        EncryptedItem rhs = ((EncryptedItem) other);
        return new EqualsBuilder().appendSuper(super.equals(other)).append(content, rhs.content).append(contentType, rhs.contentType).append(authHash, rhs.authHash).isEquals();
    }

}

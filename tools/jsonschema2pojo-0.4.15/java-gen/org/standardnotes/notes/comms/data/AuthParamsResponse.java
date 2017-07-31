
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
    "pw_func",
    "pw_alg",
    "pw_cost",
    "pw_key_size",
    "pw_salt",
    "pw_nonce"
})
public class AuthParamsResponse {

    @JsonProperty("pw_func")
    private String pwFunc;
    @JsonProperty("pw_alg")
    private String pwAlg;
    @JsonProperty("pw_cost")
    private Integer pwCost;
    @JsonProperty("pw_key_size")
    private Integer pwKeySize;
    @JsonProperty("pw_salt")
    private String pwSalt;
    @JsonProperty("pw_nonce")
    private String pwNonce;

    /**
     * 
     * @return
     *     The pwFunc
     */
    @JsonProperty("pw_func")
    public String getPwFunc() {
        return pwFunc;
    }

    /**
     * 
     * @param pwFunc
     *     The pw_func
     */
    @JsonProperty("pw_func")
    public void setPwFunc(String pwFunc) {
        this.pwFunc = pwFunc;
    }

    /**
     * 
     * @return
     *     The pwAlg
     */
    @JsonProperty("pw_alg")
    public String getPwAlg() {
        return pwAlg;
    }

    /**
     * 
     * @param pwAlg
     *     The pw_alg
     */
    @JsonProperty("pw_alg")
    public void setPwAlg(String pwAlg) {
        this.pwAlg = pwAlg;
    }

    /**
     * 
     * @return
     *     The pwCost
     */
    @JsonProperty("pw_cost")
    public Integer getPwCost() {
        return pwCost;
    }

    /**
     * 
     * @param pwCost
     *     The pw_cost
     */
    @JsonProperty("pw_cost")
    public void setPwCost(Integer pwCost) {
        this.pwCost = pwCost;
    }

    /**
     * 
     * @return
     *     The pwKeySize
     */
    @JsonProperty("pw_key_size")
    public Integer getPwKeySize() {
        return pwKeySize;
    }

    /**
     * 
     * @param pwKeySize
     *     The pw_key_size
     */
    @JsonProperty("pw_key_size")
    public void setPwKeySize(Integer pwKeySize) {
        this.pwKeySize = pwKeySize;
    }

    /**
     * 
     * @return
     *     The pwSalt
     */
    @JsonProperty("pw_salt")
    public String getPwSalt() {
        return pwSalt;
    }

    /**
     * 
     * @param pwSalt
     *     The pw_salt
     */
    @JsonProperty("pw_salt")
    public void setPwSalt(String pwSalt) {
        this.pwSalt = pwSalt;
    }

    /**
     * 
     * @return
     *     The pwNonce
     */
    @JsonProperty("pw_nonce")
    public String getPwNonce() {
        return pwNonce;
    }

    /**
     * 
     * @param pwNonce
     *     The pw_nonce
     */
    @JsonProperty("pw_nonce")
    public void setPwNonce(String pwNonce) {
        this.pwNonce = pwNonce;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(pwFunc).append(pwAlg).append(pwCost).append(pwKeySize).append(pwSalt).append(pwNonce).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof AuthParamsResponse) == false) {
            return false;
        }
        AuthParamsResponse rhs = ((AuthParamsResponse) other);
        return new EqualsBuilder().append(pwFunc, rhs.pwFunc).append(pwAlg, rhs.pwAlg).append(pwCost, rhs.pwCost).append(pwKeySize, rhs.pwKeySize).append(pwSalt, rhs.pwSalt).append(pwNonce, rhs.pwNonce).isEquals();
    }

}

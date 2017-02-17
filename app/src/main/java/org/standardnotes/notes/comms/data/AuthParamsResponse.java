
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
public class AuthParamsResponse {

    @SerializedName("pw_func")
    @Expose
    private String pwFunc;
    @SerializedName("pw_alg")
    @Expose
    private String pwAlg;
    @SerializedName("pw_cost")
    @Expose
    private Integer pwCost;
    @SerializedName("pw_key_size")
    @Expose
    private Integer pwKeySize;
    @SerializedName("pw_salt")
    @Expose
    private String pwSalt;
    @SerializedName("pw_nonce")
    @Expose
    private String pwNonce;

    /**
     * 
     * @return
     *     The pwFunc
     */
    public String getPwFunc() {
        return pwFunc;
    }

    /**
     * 
     * @param pwFunc
     *     The pw_func
     */
    public void setPwFunc(String pwFunc) {
        this.pwFunc = pwFunc;
    }

    /**
     * 
     * @return
     *     The pwAlg
     */
    public String getPwAlg() {
        return pwAlg;
    }

    /**
     * 
     * @param pwAlg
     *     The pw_alg
     */
    public void setPwAlg(String pwAlg) {
        this.pwAlg = pwAlg;
    }

    /**
     * 
     * @return
     *     The pwCost
     */
    public Integer getPwCost() {
        return pwCost;
    }

    /**
     * 
     * @param pwCost
     *     The pw_cost
     */
    public void setPwCost(Integer pwCost) {
        this.pwCost = pwCost;
    }

    /**
     * 
     * @return
     *     The pwKeySize
     */
    public Integer getPwKeySize() {
        return pwKeySize;
    }

    /**
     * 
     * @param pwKeySize
     *     The pw_key_size
     */
    public void setPwKeySize(Integer pwKeySize) {
        this.pwKeySize = pwKeySize;
    }

    /**
     * 
     * @return
     *     The pwSalt
     */
    public String getPwSalt() {
        return pwSalt;
    }

    /**
     * 
     * @param pwSalt
     *     The pw_salt
     */
    public void setPwSalt(String pwSalt) {
        this.pwSalt = pwSalt;
    }

    /**
     * 
     * @return
     *     The pwNonce
     */
    public String getPwNonce() {
        return pwNonce;
    }

    /**
     * 
     * @param pwNonce
     *     The pw_nonce
     */
    public void setPwNonce(String pwNonce) {
        this.pwNonce = pwNonce;
    }

}

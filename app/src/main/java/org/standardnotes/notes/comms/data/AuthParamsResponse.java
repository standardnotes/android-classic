
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
public class AuthParamsResponse {

    @SerializedName("pw_cost")
    @Expose
    private Integer pwCost;
    @SerializedName("pw_salt")
    @Expose
    private String pwSalt;
    @SerializedName("pw_auth")
    @Expose
    private String pwAuth;

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
     *     The pwAuth
     */
    public String getPwAuth() {
        return pwAuth;
    }

    /**
     * 
     * @param pwAuth
     *     The pw_auth
     */
    public void setPwAuth(String pwAuth) {
        this.pwAuth = pwAuth;
    }

}

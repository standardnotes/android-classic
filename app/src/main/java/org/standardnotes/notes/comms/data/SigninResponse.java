
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
public class SigninResponse {

    @SerializedName("token")
    @Expose
    private String token;

    /**
     * 
     * @return
     *     The token
     */
    public String getToken() {
        return token;
    }

    /**
     * 
     * @param token
     *     The token
     */
    public void setToken(String token) {
        this.token = token;
    }

}

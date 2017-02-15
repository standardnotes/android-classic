package org.standardnotes.notes.comms.data.export;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import org.standardnotes.notes.comms.data.AuthParamsResponse;
import org.standardnotes.notes.comms.data.EncryptedItem;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Generated;

/**
 * <p>
 */
@Generated("org.jsonschema2pojo")
public class ExportItemsEncrypted {

    @SerializedName("auth_params")
    @Expose
    private AuthParamsResponse authParams;

    @SerializedName("items")
    @Expose
    private List<EncryptedItem> items = new ArrayList<EncryptedItem>();

    public AuthParamsResponse getAuthParams() {
        return authParams;
    }

    public void setAuthParams(AuthParamsResponse authParams) {
        this.authParams = authParams;
    }

    public List<EncryptedItem> getItems() {
        return items;
    }

    public void setItems(List<EncryptedItem> items) {
        this.items = items;
    }

}

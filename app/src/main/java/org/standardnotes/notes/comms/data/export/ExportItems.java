package org.standardnotes.notes.comms.data.export;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import org.standardnotes.notes.comms.data.AuthParamsResponse;
import org.standardnotes.notes.comms.data.EncryptableItem;

import java.util.ArrayList;
import java.util.List;

public class ExportItems {

    @SerializedName("auth_params")
    @Expose
    private AuthParamsResponse authParams;

    @SerializedName("items")
    @Expose
    private List<EncryptableItem> items = new ArrayList<EncryptableItem>();

    public AuthParamsResponse getAuthParams() {
        return authParams;
    }

    public void setAuthParams(AuthParamsResponse authParams) {
        this.authParams = authParams;
    }

    public List<EncryptableItem> getItems() {
        return items;
    }

    public void setItems(List<EncryptableItem> items) {
        this.items = items;
    }

}

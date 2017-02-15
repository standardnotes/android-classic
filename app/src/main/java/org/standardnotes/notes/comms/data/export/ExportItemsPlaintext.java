package org.standardnotes.notes.comms.data.export;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Generated;

/**
 * <p>
 */
@Generated("org.jsonschema2pojo")
public class ExportItemsPlaintext {

    @SerializedName("items")
    @Expose
    private List<PlaintextItem> items = new ArrayList<PlaintextItem>();

    public List<PlaintextItem> getItems() {
        return items;
    }

    public void setItems(List<PlaintextItem> items) {
        this.items = items;
    }

}
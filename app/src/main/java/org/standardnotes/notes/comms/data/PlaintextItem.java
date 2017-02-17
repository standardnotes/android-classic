package org.standardnotes.notes.comms.data;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class PlaintextItem extends EncryptableItem {

    @SerializedName("content")
    @Expose
    private EncryptableItem content;
    @SerializedName("content_type")
    @Expose
    private String contentType;

    public PlaintextItem() {
        setDirty(null);
        setDeleted(null);
        setReferences(null);
    }

    /**
     * @return The content
     */
    public EncryptableItem getContent() {
        return content;
    }

    /**
     * @param content The content
     */
    public void setContent(EncryptableItem content) {
        this.content = content;
    }

    /**
     * @return The contentType
     */
    public String getContentType() {
        return contentType;
    }

    /**
     * @param contentType The content_type
     */
    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

}
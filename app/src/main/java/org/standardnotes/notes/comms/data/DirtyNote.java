package org.standardnotes.notes.comms.data;

import org.standardnotes.notes.comms.Crypt;

import java.util.UUID;

/**
 * Created by carl on 19/01/17.
 */

public class DirtyNote extends Note {
    public DirtyNote() {
        setOriginal(new EncryptedItem());
    }

    public DirtyNote(Note note) {
        if (note != null) {
            setText(note.getText());
            setTitle(note.getTitle());
            setOriginal(note.getOriginal());
        } else {
            EncryptedItem item = new EncryptedItem();
            item.setUuid(UUID.randomUUID().toString());
            item.setContentType(ContentType.Note.toString());
            try {
                item.setEncItemKey(Crypt.generateKey(512));
            } catch (Exception e) {
                e.printStackTrace();
            }
            setOriginal(item);
        }
    }
}

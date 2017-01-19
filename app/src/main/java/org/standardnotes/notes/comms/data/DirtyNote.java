package org.standardnotes.notes.comms.data;

/**
 * Created by carl on 19/01/17.
 */

public class DirtyNote extends Note {
    public DirtyNote() {
        setOriginal(new EncryptedItem());
    }

    public DirtyNote(Note note) {
        setText(note.getText());
        setTitle(note.getTitle());
        setOriginal(note.getOriginal());
    }
}

package org.standardnotes.notes.frag

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import org.standardnotes.notes.R
import org.standardnotes.notes.SApplication
import org.standardnotes.notes.comms.data.Note

import kotlinx.android.synthetic.main.frag_note.*
import org.standardnotes.notes.comms.data.DirtyNote

/**
 * Created by carl on 15/01/17.
 */

class NoteFragment : Fragment() {

    var note: Note? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        note = SApplication.instance!!.gson.fromJson(arguments?.getString("note"), Note::class.java)
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater!!.inflate(R.layout.frag_note, container, false)
        return view
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        titleEdit.setText(note?.title)
        bodyEdit.setText(note?.text)
        titleLayout.isHintAnimationEnabled = true
    }

    override fun onPause() {
        super.onPause()
        if (activity.isFinishing) {
            val dn = DirtyNote(note)
            dn.title = titleEdit.text.toString()
            dn.text = bodyEdit.text.toString()
            SApplication.instance!!.noteStore.setDirty(dn)
        }
    }


//    override fun onStop() {
//        super.onStop()
//        val uSync = UploadSyncItems()
//        val thisItem = note!!.encrypted()
//        uSync.items.add(thisItem)
//        SApplication.instance!!.comms.api.sync(uSync)
//    }

//    fun Note.encrypted(): EncryptedItem {
//        val item = EncryptedItem()
//        item.contentType = original.contentType
//        item.createdAt = original.createdAt
//        item.
//    }

}

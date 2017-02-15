package org.standardnotes.notes.frag

import android.content.Intent
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v4.app.Fragment
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.PopupMenu
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.frag_note_list.*
import kotlinx.android.synthetic.main.item_note.view.*
import org.joda.time.format.DateTimeFormat
import org.standardnotes.notes.NoteActivity
import org.standardnotes.notes.R
import org.standardnotes.notes.SApplication
import org.standardnotes.notes.comms.SyncManager
import org.standardnotes.notes.comms.data.Note
import java.util.*


class NoteListFragment : Fragment(), SyncManager.SyncListener {

    private val REQ_EDIT_NOTE: Int = 1

    val adapter: Adapter by lazy { Adapter() }

    var notes = ArrayList<Note>()

    var currentSnackbar: Snackbar? = null

    var lastTouchedX: Int? = null
    var lastTouchedY: Int? = null

    companion object {
        const val EXTRA_NOTE_ID = "noteId"
        const val EXTRA_X_COOR = "xCoor"
        const val EXTRA_Y_COOR = "yCoor"
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val view = inflater!!.inflate(R.layout.frag_note_list, container, false)
        return view
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        SyncManager.subscribe(this)
        list.layoutManager = LinearLayoutManager(activity, LinearLayoutManager.VERTICAL, false)
        list.addItemDecoration(DividerItemDecoration(activity, LinearLayoutManager.VERTICAL))
        swipeRefreshLayout.setColorSchemeResources(
                R.color.colorPrimary,
                R.color.colorAccent)
        swipeRefreshLayout.setOnRefreshListener { SyncManager.sync() }
        notes = ArrayList(SApplication.instance!!.noteStore.notesList)
        SyncManager.sync()
        list.adapter = adapter
    }

    override fun onDestroyView() {
        super.onDestroyView()
        SyncManager.unsubscribe(this)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
//        if (resultCode == RESULT_OK) {
        if (requestCode == REQ_EDIT_NOTE) {
            notes = ArrayList(SApplication.instance!!.noteStore.notesList)
            adapter.notifyDataSetChanged()
            if (SApplication.instance!!.noteStore.notesToSaveCount() > 0) {
                SyncManager.sync()
            }
        }
//        }
    }

    override fun onSyncStarted() {
        swipeRefreshLayout.isRefreshing = true
        currentSnackbar?.dismiss()
    }

    override fun onSyncCompleted(syncedNotes: List<Note>) {
        notes = ArrayList(syncedNotes)
        swipeRefreshLayout.isRefreshing = false
        adapter.notifyDataSetChanged()
        currentSnackbar?.dismiss()
    }

    override fun onSyncFailed() {
        swipeRefreshLayout.isRefreshing = false
        currentSnackbar = Snackbar.make(activity.rootView, R.string.error_fail_sync, Snackbar.LENGTH_INDEFINITE)
                .setAction(R.string.sync_retry, {
                    SyncManager.sync()
                })
        currentSnackbar!!.show()
    }

    fun startNewNote(x: Int, y: Int) {
        val intent: Intent = Intent(activity, NoteActivity::class.java)
        intent.putExtra(EXTRA_X_COOR, x)
        intent.putExtra(EXTRA_Y_COOR, y)
        startActivityForResult(intent, REQ_EDIT_NOTE)
    }

    inner class NoteHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        var note: Note? = null
            get
            set(value) {
                field = value
                itemView.title.text = note?.title
                itemView.date.text = DateTimeFormat.shortDateTime().print(note?.updatedAt)
                var noteText = note?.text ?: ""
                noteText = noteText.substring(0, Math.min(256, noteText.length))
                noteText.replace('\n', ' ')
                itemView.text.text = noteText
                itemView.synced.visibility = if (note?.dirty == true) View.VISIBLE else View.INVISIBLE
            }

        init {
            itemView.setOnTouchListener({ v, event ->
                if (event.action == MotionEvent.ACTION_UP) {
                    lastTouchedX = event.rawX.toInt()
                    lastTouchedY = event.rawY.toInt()
                }
                false
            })
            itemView.setOnClickListener {
                val intent: Intent = Intent(activity, NoteActivity::class.java)
                intent.putExtra(EXTRA_NOTE_ID, note?.uuid)
                intent.putExtra(EXTRA_X_COOR, lastTouchedX)
                intent.putExtra(EXTRA_Y_COOR, lastTouchedY)
                startActivityForResult(intent, REQ_EDIT_NOTE)
            }
            itemView.setOnLongClickListener {
                val popup = PopupMenu(activity, itemView)
                popup.menu.add(activity.getString(R.string.action_delete))
                popup.setOnMenuItemClickListener {
                    SApplication.instance!!.noteStore.deleteItem(note!!.uuid)
                    notes = ArrayList(SApplication.instance!!.noteStore.notesList)
                    adapter.notifyDataSetChanged()
                    SyncManager.sync()
                    true
                }
                popup.show()
                true
            }
        }
    }

    inner class Adapter : RecyclerView.Adapter<NoteHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): NoteHolder {
            return NoteHolder(LayoutInflater.from(activity).inflate(R.layout.item_note, parent, false))
        }

        override fun getItemCount(): Int {
            return notes.count()
        }

        override fun onBindViewHolder(holder: NoteHolder, position: Int) {
            val note: Note = notes[position]
            holder.note = note
        }

    }
}

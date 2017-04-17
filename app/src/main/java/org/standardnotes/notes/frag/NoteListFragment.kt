package org.standardnotes.notes.frag

import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v4.app.Fragment
import android.support.v7.app.AlertDialog
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.PopupMenu
import android.support.v7.widget.RecyclerView
import android.text.TextUtils
import android.view.*
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.frag_note_list.*
import kotlinx.android.synthetic.main.frag_note_list.view.*
import kotlinx.android.synthetic.main.item_note.view.*
import org.joda.time.format.DateTimeFormat
import org.standardnotes.notes.R
import org.standardnotes.notes.SApplication
import org.standardnotes.notes.comms.SyncManager
import org.standardnotes.notes.comms.data.Note
import java.util.*

class NoteListFragment : Fragment(), SyncManager.SyncListener {

    val adapter: Adapter by lazy { Adapter() }

    var notes = ArrayList<Note>()
    var tagId = ""
    var selectedTagId = ""

    var currentSnackbar: Snackbar? = null

    var lastTouchedX: Int? = null
    var lastTouchedY: Int? = null

    lateinit var onNewNoteListener: OnNewNoteClickListener

    companion object {
        const val EXTRA_NOTE_ID = "noteId"
        const val EXTRA_TAG_ID = "tagId"
        const val EXTRA_X_COOR = "xCoor"
        const val EXTRA_Y_COOR = "yCoor"
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.frag_note_list, container, false)
        setHasOptionsMenu(true)
        setFabClickListener(view)

        return view
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        SyncManager.subscribe(this)
        list_note.layoutManager = LinearLayoutManager(activity, LinearLayoutManager.VERTICAL, false)
        list_note.addItemDecoration(DividerItemDecoration(activity, LinearLayoutManager.VERTICAL))
        swipeRefreshLayout.setColorSchemeResources(
                R.color.colorPrimary,
                R.color.colorAccent)
        swipeRefreshLayout.setOnRefreshListener { SyncManager.sync() }
        SyncManager.sync()
        list_note.adapter = adapter
    }

    override fun onDestroyView() {
        super.onDestroyView()
        SyncManager.unsubscribe(this)
    }

    override fun onResume() {
        super.onResume()
        refreshNotesForTag() // This is too often and slow for large datasets, but necessary until we have an event to trigger refresh
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.note_list, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            R.id.new_note -> startNewNote(selectedTagId)
        }

        return super.onOptionsItemSelected(item)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString("tagId", tagId)
        outState.putString("tag", selectedTagId)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        if (savedInstanceState != null) {
            tagId = savedInstanceState.getString("tagId")
            selectedTagId = savedInstanceState.getString("tag")
        }
    }

    override fun onSyncStarted() {
        swipeRefreshLayout.isRefreshing = true
        currentSnackbar?.dismiss()
    }

    override fun onSyncCompleted() {
        swipeRefreshLayout.isRefreshing = false
        currentSnackbar?.dismiss()
    }

    fun setFabClickListener(view: View) {
        if (view.fab_new_note == null) {
            return
        }

        var lastX: Int? = null
        var lastY: Int? = null
        view.fab_new_note.setOnTouchListener({ v, event ->
            if (event.action == MotionEvent.ACTION_UP) {
                lastX = event.rawX.toInt()
                lastY = event.rawY.toInt()
            }
            false
        })
        view.fab_new_note.setOnClickListener { view ->
            startNewNote(selectedTagId)
        }
    }

    fun refreshNotesForTag(uuid: String? = null) {
        if (uuid == null) { // In-place refresh after delete
            refreshNotesForTag(tagId)
            return
        }
        val noteList = if (TextUtils.isEmpty(uuid))
            SApplication.instance.noteStore.getAllNotes()
        else
            SApplication.instance.noteStore.getNotesForTag(uuid)
        notes = ArrayList(noteList)
        adapter.notifyDataSetChanged()
        tagId = uuid // Save for future use
    }

    override fun onSyncFailed() {
        swipeRefreshLayout.isRefreshing = false
        // TODO this always assumes it's a network error, but the server may have errored or the local store may have failed
        currentSnackbar = Snackbar.make(activity.rootView, R.string.error_fail_sync, Snackbar.LENGTH_INDEFINITE)
                .setAction(R.string.sync_retry, {
                    SyncManager.sync()
                })
        currentSnackbar!!.show()
    }

    fun startNewNote(uuid: String) {
        onNewNoteListener.newNoteListener(uuid)
    }

    interface OnNewNoteClickListener {
        fun newNoteListener(uuid: String)
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
                onNewNoteListener.newNoteListener(note!!.uuid)
            }
            itemView.setOnLongClickListener {
                val popup = PopupMenu(activity, itemView)
                popup.menu.add(activity.getString(R.string.action_delete))
                popup.setOnMenuItemClickListener {
                    AlertDialog.Builder(activity)
                            .setTitle(R.string.title_delete_confirm)
                            .setMessage(R.string.prompt_are_you_sure)
                            .setPositiveButton(R.string.action_delete, { dialogInterface, i ->
                                SApplication.instance.noteStore.deleteItem(note!!.uuid)
                                refreshNotesForTag()
                                SyncManager.sync()
                            })
                            .setNegativeButton(R.string.action_cancel, null)
                            .show()
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

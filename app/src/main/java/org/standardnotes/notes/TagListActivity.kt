package org.standardnotes.notes

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.view.inputmethod.EditorInfo
import android.widget.CheckBox
import android.widget.EditText
import android.widget.TextView
import com.google.gson.reflect.TypeToken
import kotlinx.android.synthetic.main.activity_tags.*
import org.joda.time.DateTime
import org.standardnotes.notes.comms.Crypt
import org.standardnotes.notes.comms.SyncManager
import org.standardnotes.notes.comms.data.Tag
import java.util.*

const val EXTRA_TAGS: String = "refTags"

class TagListActivity : BaseActivity() {

    lateinit var selectedTags: Set<Tag>
    lateinit var tags: List<Tag>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tags)

        val listType = object : TypeToken<List<Tag>>() {}.type
        val selectedTagsList: List<Tag> = app.gson.fromJson(
                if (savedInstanceState == null) intent.getStringExtra(EXTRA_TAGS) else savedInstanceState.getString(EXTRA_TAGS),
                listType)
        selectedTags = selectedTagsList.toSet()
        tags = app.noteStore.getAllTags(false)

        list_tag.adapter = Adapter()
        list_tag.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)

        fab_new_tag.setOnClickListener {
            val layout = LayoutInflater.from(this).inflate(R.layout.view_new_tag, null, false)
            val input = layout.findViewById(R.id.tag) as EditText
            val dialog = AlertDialog.Builder(this).setTitle(R.string.prompt_new_tag)
                    .setNegativeButton(R.string.action_cancel, null)
                    .setPositiveButton(R.string.action_ok, { dialogInterface, i ->
                        val newTag = newTag()
                        newTag.title = input.text.toString()
                        newTag.dirty = true
                        app.noteStore.putTag(newTag.uuid, newTag)
                        SyncManager.sync()
                        tags = app.noteStore.getAllTags(false)
                        list_tag.adapter.notifyDataSetChanged()
                    })
                    .setView(layout)
                    .show()
            dialog.window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE)
            val okbutton = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
            input.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                    //
                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    okbutton.isEnabled = !input.text.isBlank()
                }

                override fun afterTextChanged(s: Editable?) {
                    //
                }
            })
            input.setOnEditorActionListener(TextView.OnEditorActionListener { v, actionId, event ->
                if (actionId == EditorInfo.IME_ACTION_DONE && okbutton.isEnabled) {
                    okbutton.performClick()
                    return@OnEditorActionListener true
                }
                false
            })
            okbutton.isEnabled = false
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(EXTRA_TAGS, app.gson.toJson(selectedTags.toList()))
    }

    override fun finish() {
        val data = Intent()
        data.putExtra(EXTRA_TAGS, app.gson.toJson(selectedTags.toList()))
        setResult(Activity.RESULT_OK, data)
        super.finish()
    }

    inner class TagHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        var tag: Tag? = null
            get
            set(value) {
                field = value
                title.text = tag?.title
                title.setOnCheckedChangeListener(null)
                title.isChecked = selectedTags.filter { it.uuid == tag?.uuid }.isNotEmpty()
                title.setOnCheckedChangeListener { compoundButton, b ->
                    if (compoundButton.isChecked) {
                        selectedTags += tag!!
                    } else {
                        selectedTags = selectedTags.filterNot { it.uuid == tag?.uuid }.toSet()
                    }
                }
            }
        private val title: CheckBox = itemView.findViewById(R.id.title) as CheckBox

    }

    inner class Adapter : RecyclerView.Adapter<TagHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): TagHolder {
            return TagHolder(LayoutInflater.from(this@TagListActivity).inflate(R.layout.item_tag, parent, false))
        }

        override fun getItemCount(): Int {
            return tags.count()
        }

        override fun onBindViewHolder(holder: TagHolder, position: Int) {
            val tag = tags[position]
            holder.tag = tag
        }

    }

    fun newTag(): Tag {
        // Move to a factory
        val tag = Tag()
        tag.uuid = UUID.randomUUID().toString()
        tag.encItemKey = Crypt.generateEncryptedKey(512)
        tag.createdAt = DateTime.now()
        tag.updatedAt = tag.createdAt
        return tag
    }

}
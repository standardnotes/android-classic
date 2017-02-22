package org.standardnotes.notes

import android.os.Bundle
import kotlinx.android.synthetic.main.activity_debug.*
import org.joda.time.DateTime
import org.standardnotes.notes.comms.data.ContentType
import org.standardnotes.notes.comms.data.Note
import org.standardnotes.notes.comms.data.Reference
import org.standardnotes.notes.comms.data.Tag
import java.util.*

/**
 * Created by carl on 22/02/17.
 */
class DebugActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_debug)

        create_large_data.setOnClickListener {

            fun createString(length: Int): String {
                val chars = "abcdefghijklmnopqrstuvwxyz  ".toCharArray()
                val sb = StringBuilder(length)
                val random = Random()
                for (i in 0..length) {
                    val c = chars[random.nextInt(chars.size)]
                    sb.append(c)
                }
                return sb.toString()
            }

            fun createLargeNote(tagId: String?): Note {
                val ns = SApplication.instance.noteStore
                val n = Note()
                n.text = createString(50000)
                n.encItemKey = "123"
                n.uuid = UUID.randomUUID().toString()
                n.title = n.uuid
                val time = DateTime.now()
                n.createdAt = time
                n.updatedAt = time
                n.references = ArrayList()
                if (tagId != null) {
                    val ref = Reference()
                    ref.contentType = ContentType.Tag.toString()
                    ref.uuid = tagId
                    n.references.add(ref)
                }
                ns.putNote(n.uuid, n)

                return n
            }

            fun createTag(): Tag {
                val ns = SApplication.instance.noteStore
                val t = Tag()
                t.uuid = UUID.randomUUID().toString()
                t.title = t.uuid
                val time = DateTime.now()
                t.createdAt = time
                t.updatedAt = time
                t.references = ArrayList()
                ns.putTag(t.uuid, t)
                return t
            }

            if (packageName != "org.standardnotes.notes.buildfortest") {
                throw Exception("only run this when connected to a staging server")
            }

//            val ns = SApplication.instance.noteStore
            val tagIds = ArrayList<String>()
            for (i in 1..20) {
                tagIds.add(createTag().uuid)
            }

            val rand = Random()
            for (i in 1..200) {
                createLargeNote(tagIds[rand.nextInt(tagIds.size)])
            }


        }

        clear.setOnClickListener {
            SApplication.instance.noteStore.deleteAll()
        }
    }

}
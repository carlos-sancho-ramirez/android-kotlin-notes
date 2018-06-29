package sword.notes.android

import android.app.Activity
import android.os.Bundle
import android.widget.ListView

class NoteListActivity : Activity() {

    val listView by lazy {
        findViewById<ListView>(R.id.listView)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.note_list_activity)

        val notes = listOf(NoteListItem("Note 1"), NoteListItem("Note 2"))
        listView.adapter = NoteListAdapter(notes)
    }
}

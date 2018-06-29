package sword.notes.android

import android.app.Activity
import android.os.Bundle
import android.widget.ListView

class NoteListActivity : Activity() {

    private var listView: ListView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.note_list_activity)

        val notes = listOf(NoteListItem("Note 1"), NoteListItem("Note 2"), NoteListItem("Note 3"))
        listView = findViewById(R.id.listView)
        listView?.adapter = NoteListAdapter(notes)
    }
}

package sword.notes.android

import android.app.Activity
import android.os.Bundle

class NoteListActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.note_list_activity)
    }
}

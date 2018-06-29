package sword.notes.android

import android.app.Activity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.ListView
import java.io.File

class NoteListActivity : Activity() {

    val notesDirName = "notes"

    val listView by lazy {
        findViewById<ListView>(R.id.listView)
    }

    val notesDir: File by lazy {
        val notesDir = File(filesDir, notesDirName)
        notesDir.mkdir()
        notesDir
    }

    private fun getNotesInfo() : List<NoteListItem> {
        return notesDir.list().map({ name -> NoteListItem(name) })
    }

    private fun createNote(name: String) {
        if (!File(notesDir, name).createNewFile()) {
            throw AssertionError()
        }
    }

    private fun updateList() {
        listView.adapter = NoteListAdapter(getNotesInfo())
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.note_list_activity)
    }

    override fun onResume() {
        super.onResume()
        updateList()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menu!!.add("New")
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        createNote(System.currentTimeMillis().toString())
        updateList()
        return true
    }
}

package sword.notes.android

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle

private const val argNoteId = "nId"

class NoteEditorActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.note_editor_activity)
    }

    companion object {

        fun open(context: Context, noteId: String): Unit {
            val intent = Intent(context, NoteEditorActivity::class.java)
            intent.putExtra(argNoteId, noteId)
            context.startActivity(intent)
        }
    }
}

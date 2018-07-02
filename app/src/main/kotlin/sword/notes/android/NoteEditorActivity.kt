package sword.notes.android

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.TextView
import android.widget.Toast
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.PrintWriter

private const val argNoteId = "nId"
private const val showingLeaveDialogKey = "sld"

class NoteEditorActivity : Activity() {

    private var showingLeaveDialog: Boolean = false

    val textField by lazy {
        findViewById<TextView>(R.id.textField)
    }

    val notesDir: File by lazy {
        getNotesDir(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.note_editor_activity)

        actionBar.title = intent.getStringExtra(argNoteId)
        if (savedInstanceState == null) {
            val file = File(notesDir, intent.getStringExtra(argNoteId))
            val fileLength = file.length().toInt()
            val content = ByteArray(fileLength)
            val inStream = FileInputStream(file)
            inStream.read(content, 0, fileLength)
            inStream.close()
            textField.text = String(content)
        }
        else if (savedInstanceState.getBoolean(showingLeaveDialogKey)) {
            showingLeaveDialog = true
            showLeaveDialog()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menu!!.add(R.string.optionSave)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        val file = File(notesDir, intent.getStringExtra(argNoteId))
        val outStream = PrintWriter(FileOutputStream(file, false))
        outStream.print(textField.text.toString())
        outStream.close()

        Toast.makeText(this, R.string.saveFeedback, Toast.LENGTH_SHORT).show()
        return true
    }

    override fun onSaveInstanceState(outState: Bundle?) {
        super.onSaveInstanceState(outState)
        outState!!.putBoolean(showingLeaveDialogKey, showingLeaveDialog)
    }

    override fun onBackPressed(): Unit {
        if (!showingLeaveDialog) {
            showingLeaveDialog = true
            showLeaveDialog()
        }
        else {
            super.onBackPressed()
        }
    }

    private fun showLeaveDialog(): Unit {
        AlertDialog.Builder(this)
                .setMessage(R.string.leaveConfirmationMessage)
                .setPositiveButton(android.R.string.yes) { _, _ -> finish() }
                .setNegativeButton(android.R.string.no) { _, _ -> showingLeaveDialog = false }
                .setOnCancelListener { _ -> showingLeaveDialog = false}
                .create().show()
    }

    companion object {
        fun open(context: Context, noteId: String): Unit {
            val intent = Intent(context, NoteEditorActivity::class.java)
            intent.putExtra(argNoteId, noteId)
            context.startActivity(intent)
        }
    }
}

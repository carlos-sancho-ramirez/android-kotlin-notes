package sword.notes.android

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.os.Parcel
import android.os.Parcelable
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.text.format.DateFormat
import android.view.*
import android.widget.*
import java.io.File
import java.util.*

class NoteListActivityState() : Parcelable {
    var intrisicState: Int = intrinsicStateNormal
    val selectedItems = BitSet()
    var noteTitle: String? = null

    private constructor(parcel: Parcel) : this() {
        intrisicState = parcel.readInt()
        if (intrisicState == intrinsicStateSelection || intrisicState == intrinsicStateDeleteConfirmation) {
            val intWords = parcel.readInt()
            for (i in 0 until intWords) {
                val word = parcel.readInt()
                for (j in 0..31) {
                    if ((word and (1 shl j)) != 0) {
                        selectedItems.set(i * 32 + j)
                    }
                }
            }
        }
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeInt(intrisicState)
        if (intrisicState == intrinsicStateSelection|| intrisicState == intrinsicStateDeleteConfirmation) {
            val length = selectedItems.length()
            val intWords = (length + 31) / 32
            dest.writeInt(intWords)

            for (i in 0 until intWords) {
                var word = 0
                for (j in 0..31) {
                    if (selectedItems.get(i * 32 + j)) {
                        word = word or (1 shl j)
                    }
                }
                dest.writeInt(word)
            }
        }
    }

    override fun describeContents() = 0

    companion object {
        const val intrinsicStateNormal = 0
        const val intrinsicStateSelection = 1
        const val intrinsicStateDeleteConfirmation = 2

        @JvmField val CREATOR = object : Parcelable.Creator<NoteListActivityState> {
            override fun createFromParcel(parcel: Parcel) = NoteListActivityState(parcel)
            override fun newArray(size: Int) = arrayOfNulls<NoteListActivityState?>(size)
        }
    }
}

private const val stateKey = "state"
private const val requestCodeEditor = 1

class NoteListActivity : Activity(), AdapterView.OnItemClickListener, AbsListView.MultiChoiceModeListener, TextWatcher {

    private val listView by lazy {
        findViewById<ListView>(R.id.listView)
    }

    private val notesDir: File by lazy {
        getNotesDir(this)
    }

    var state = NoteListActivityState()
    var actionMode: ActionMode? = null
    var justUpdated: Boolean = false

    private fun getDateInfo(noteId: String): String {
        val millis = File(notesDir, noteId).lastModified()
        if (millis > 0) {
            val dateFormat = DateFormat.getDateFormat(this)
            val timeFormat = DateFormat.getTimeFormat(this)
            val calendar = Calendar.getInstance()
            calendar.timeInMillis = millis
            return dateFormat.format(calendar.time) + ' ' + timeFormat.format(calendar.time)
        }

        return ""
    }

    private fun getNotesInfo() : List<NoteListItem> {
        return notesDir.list().map { name -> NoteListItem(name, getDateInfo(name)) }
    }

    private fun createNote(name: String?) {
        if (TextUtils.isEmpty(name) || !File(notesDir, name).createNewFile()) {
            Toast.makeText(this, R.string.unableToCreate, Toast.LENGTH_SHORT).show()
        }
        else {
            updateList()
        }
    }

    private fun updateList() {
        listView.adapter = NoteListAdapter(getNotesInfo())
        listView.onItemClickListener = this
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.note_list_activity)

        if (savedInstanceState != null) {
            state = savedInstanceState.getParcelable(stateKey)
        }

        listView.choiceMode = ListView.CHOICE_MODE_MULTIPLE_MODAL
        listView.setMultiChoiceModeListener(this)
        updateList()
        justUpdated = true

        var nextBit = state.selectedItems.nextSetBit(0)
        while (nextBit >= 0) {
            listView.setSelection(nextBit)
            nextBit = state.selectedItems.nextSetBit(nextBit + 1)
        }

        if (state.intrisicState == NoteListActivityState.intrinsicStateDeleteConfirmation) {
            showDeleteConfirmationDialog()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.note_list_activity, menu)
        return true
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == requestCodeEditor && !justUpdated) {
            updateList()
        }
    }

    override fun onResume() {
        super.onResume()
        justUpdated = false
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        return when (item!!.itemId) {
            R.id.optionNew -> {
                showCreateDialog()
                true
            }
            R.id.optionAbout -> {
                AboutActivity.open(this)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onItemClick(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        val noteId = (parent!!.adapter as NoteListAdapter).items[position].title
        NoteEditorActivity.open(this, requestCodeEditor, noteId)
    }

    override fun onActionItemClicked(mode: ActionMode?, item: MenuItem?): Boolean {
        state.intrisicState = NoteListActivityState.intrinsicStateDeleteConfirmation
        showDeleteConfirmationDialog()
        return true
    }

    override fun onItemCheckedStateChanged(mode: ActionMode?, position: Int, id: Long, checked: Boolean) {
        if (checked) {
            state.intrisicState = NoteListActivityState.intrinsicStateSelection
            state.selectedItems.set(position)
        }
        else {
            state.selectedItems.clear(position)
            if (state.selectedItems.isEmpty) {
                state.intrisicState = NoteListActivityState.intrinsicStateNormal
            }
        }
    }

    override fun onCreateActionMode(mode: ActionMode?, menu: Menu?): Boolean {
        actionMode = mode
        menuInflater.inflate(R.menu.list_action_mode, menu)
        return true
    }

    override fun onPrepareActionMode(mode: ActionMode?, menu: Menu?) = false

    override fun onDestroyActionMode(mode: ActionMode?) {
        actionMode = null
        state.selectedItems.clear()
        state.intrisicState = NoteListActivityState.intrinsicStateNormal
    }

    override fun onSaveInstanceState(outState: Bundle?) {
        super.onSaveInstanceState(outState)
        outState!!.putParcelable(stateKey, state)
    }

    private fun showCreateDialog() {
        val dialog = AlertDialog.Builder(this)
                .setTitle(R.string.createDialogTitle)
                .setPositiveButton(android.R.string.yes) { _, _ -> createNote(state.noteTitle) }
                .setOnCancelListener {
                    state.intrisicState = NoteListActivityState.intrinsicStateNormal
                    state.noteTitle = null
                }
                .create()

        val view = LayoutInflater.from(dialog.context).inflate(R.layout.create_dialog, null, false)
        val textField = view.findViewById<EditText>(R.id.textField)
        textField.addTextChangedListener(this)
        dialog.setView(view)
        dialog.show()
    }

    private fun showDeleteConfirmationDialog() {
        AlertDialog.Builder(this)
                .setMessage(R.string.deleteConfirmationMessage)
                .setPositiveButton(android.R.string.yes) { _, _ -> deleteNotes() }
                .setOnCancelListener { state.intrisicState = NoteListActivityState.intrinsicStateNormal }
                .create().show()
    }

    private fun deleteNotes() {
        val items = (listView.adapter as NoteListAdapter).items
        var position = state.selectedItems.nextSetBit(0)
        while (position >= 0) {
            if (!File(notesDir, items[position].title).delete()) {
                throw AssertionError()
            }
            position = state.selectedItems.nextSetBit(position + 1)
        }

        Toast.makeText(this, R.string.deleteFeedback, Toast.LENGTH_SHORT).show()
        actionMode?.finish()
        actionMode = null
        updateList()
    }

    override fun afterTextChanged(s: Editable?) {
        state.noteTitle = s.toString()
    }

    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
        // Nothing to be done
    }

    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
        // Nothing to be done
    }
}

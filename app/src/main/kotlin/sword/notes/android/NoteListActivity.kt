package sword.notes.android

import android.app.Activity
import android.os.Bundle
import android.os.Parcel
import android.os.Parcelable
import android.view.ActionMode
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.AbsListView
import android.widget.AdapterView
import android.widget.ListView
import java.io.File
import java.util.*

class NoteListActivityState() : Parcelable {
    var intrisicState: Int = intrinsicStateNormal
    val selectedItems = BitSet()

    private constructor(parcel: Parcel) : this() {
        intrisicState = parcel.readInt()
        if (intrisicState == intrinsicStateSelection) {
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
        if (intrisicState == intrinsicStateSelection) {
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

        @JvmField val CREATOR = object : Parcelable.Creator<NoteListActivityState> {
            override fun createFromParcel(parcel: Parcel) = NoteListActivityState(parcel)
            override fun newArray(size: Int) = arrayOfNulls<NoteListActivityState?>(size)
        }
    }
}

private const val stateKey = "state"

class NoteListActivity : Activity(), AdapterView.OnItemClickListener, AbsListView.MultiChoiceModeListener {

    val listView by lazy {
        findViewById<ListView>(R.id.listView)
    }

    val notesDir: File by lazy {
        getNotesDir(this)
    }

    var state = NoteListActivityState()

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

        var nextBit = state.selectedItems.nextSetBit(0)
        while (nextBit >= 0) {
            listView.setSelection(nextBit)
            nextBit = state.selectedItems.nextSetBit(nextBit + 1)
        }
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

    override fun onItemClick(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        val noteId = (parent!!.adapter as NoteListAdapter).items[position].title
        NoteEditorActivity.open(this, noteId)
    }

    override fun onActionItemClicked(mode: ActionMode?, item: MenuItem?): Boolean {
        mode!!.finish()
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
        menu!!.add("Delete")
        return true
    }

    override fun onPrepareActionMode(mode: ActionMode?, menu: Menu?) = false

    override fun onDestroyActionMode(mode: ActionMode?) {
        state.selectedItems.clear()
        state.intrisicState = NoteListActivityState.intrinsicStateNormal
    }

    override fun onSaveInstanceState(outState: Bundle?) {
        super.onSaveInstanceState(outState)
        outState!!.putParcelable(stateKey, state)
    }
}

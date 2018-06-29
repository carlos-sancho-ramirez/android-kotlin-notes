package sword.notes.android

import android.content.Context
import java.io.File

const val notesDirName = "notes"

fun getNotesDir(context: Context): File {
    val notesDir = File(context.filesDir, notesDirName)
    notesDir.mkdir()
    return notesDir
}

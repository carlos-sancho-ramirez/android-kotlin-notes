package sword.notes.android

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView

class NoteListItem(val title: String)

class NoteListAdapter(val items: List<NoteListItem>) : BaseAdapter() {

    var inflater: LayoutInflater? = null

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view: View
        if (convertView == null) {
            if (inflater == null) {
                inflater = LayoutInflater.from(parent!!.context)
            }
            view = inflater!!.inflate(R.layout.note_list_entry, parent, false)
        }
        else {
            view = convertView
        }

        val titleView: TextView = view.findViewById(R.id.title)
        titleView.text = items[position].title

        return view
    }

    override fun getItem(position: Int): Any {
        return items[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getCount(): Int {
        return items.size
    }
}

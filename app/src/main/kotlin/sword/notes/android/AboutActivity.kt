package sword.notes.android

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.TextView

class AboutActivity : android.app.Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.about_activity)

        val versionField = findViewById<TextView>(R.id.version)
        versionField.text = getString(R.string.appVersion, BuildConfig.VERSION_NAME)
    }

    companion object {
        fun open(context: Context) {
            val intent = Intent(context, AboutActivity::class.java)
            context.startActivity(intent)
        }
    }
}

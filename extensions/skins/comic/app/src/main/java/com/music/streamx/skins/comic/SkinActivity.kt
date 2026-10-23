package com.music.streamx.skins.comic
import android.app.Activity
import android.os.Bundle
import android.widget.*
/** Settings render in the host, where the skin's live color scheme and preferences belong. */
class SkinActivity: Activity() {
    override fun onCreate(state: Bundle?) {
        super.onCreate(state)
        val body=LinearLayout(this).apply {orientation=LinearLayout.VERTICAL;setPadding(48,64,48,48)}
        body.addView(TextView(this).apply {text="Comic";textSize=28f})
        body.addView(TextView(this).apply {text="In BitChord, open Settings → Plugins & Skins → Skins and apply comic. Open its Settings to choose Paper, Ink, or Follow app mode. Fonts are bundled for offline use.";textSize=18f})
        body.addView(Button(this).apply {text="Open BitChord";setOnClickListener {packageManager.getLaunchIntentForPackage("com.music.streamx")?.let(::startActivity)}})
        setContentView(body)
    }
}

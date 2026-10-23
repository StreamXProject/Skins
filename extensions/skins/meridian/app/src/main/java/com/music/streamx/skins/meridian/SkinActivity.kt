package com.music.streamx.skins.meridian
import android.app.Activity
import android.os.Bundle
import android.widget.*
/** Settings render in the host, where the skin's live color scheme and preferences belong. */
class SkinActivity: Activity() {
    override fun onCreate(state: Bundle?) {
        super.onCreate(state)
        val body=LinearLayout(this).apply {orientation=LinearLayout.VERTICAL;setPadding(48,64,48,48)}
        body.addView(TextView(this).apply {text="Meridian";textSize=28f})
        body.addView(TextView(this).apply {text="In BitChord, open Settings → Plugins & Skins → Skins, apply Meridian, then open Settings to adjust the appearance. Meridian uses an original editorial layout and animated dial motifs.";textSize=18f})
        body.addView(Button(this).apply {text="Open BitChord";setOnClickListener {packageManager.getLaunchIntentForPackage("com.music.streamx")?.let(::startActivity)}})
        setContentView(body)
    }
}

package com.music.streamx.skins.applemusic
import android.app.Activity
import android.os.Bundle
import android.widget.TextView
class SkinActivity: Activity() {
    override fun onCreate(saved: Bundle?) { super.onCreate(saved)
        setContentView(TextView(this).apply {text="Apple Music skin\n\nOpen StreamX → Settings → Plugins & Skins → Skins to apply. Player colors follow the artwork.";textSize=18f;setPadding(48,64,48,48)})
    }
}

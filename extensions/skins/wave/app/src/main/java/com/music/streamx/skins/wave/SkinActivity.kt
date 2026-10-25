package com.music.streamx.skins.wave
import android.app.Activity
import android.os.Bundle
import android.widget.TextView
class SkinActivity : Activity() {
    override fun onCreate(saved: Bundle?) { super.onCreate(saved)
        setContentView(TextView(this).apply { text = "Wave skin\n\nOpen StreamX → Sources → Skins to apply."; textSize = 18f; setPadding(48, 64, 48, 48) })
    }
}

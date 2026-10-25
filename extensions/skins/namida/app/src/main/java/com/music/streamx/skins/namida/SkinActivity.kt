package com.music.streamx.skins.namida

import android.app.Activity
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.view.Gravity
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView

/** This APK supplies executable UI and assets; StreamX owns playback and accounts. */
class SkinActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val density = resources.displayMetrics.density
        val body = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_VERTICAL
            val gutter = (24 * density).toInt()
            setPadding(gutter, gutter, gutter, gutter)
            setBackgroundColor(Color.rgb(22, 19, 15))
        }
        body.addView(TextView(this).apply {
            text = "Namida"; textSize = 32f; setTextColor(Color.WHITE)
            typeface = Typeface.createFromAsset(assets, "streamx/fonts/LexendDeca-SemiBold.ttf")
        })
        body.addView(TextView(this).apply {
            text = "Home, library, search, player and settings.\n\nApply Namida in StreamX → Settings → Skins & Plugins."
            textSize = 16f; setTextColor(Color.LTGRAY); setPadding(0, (24*density).toInt(), 0, (24*density).toInt())
            typeface = Typeface.createFromAsset(assets, "streamx/fonts/LexendDeca-Regular.ttf")
        })
        body.addView(Button(this).apply {
            text = "Open StreamX"; setTextColor(Color.BLACK); backgroundTintList = android.content.res.ColorStateList.valueOf(Color.rgb(201, 165, 127))
            setOnClickListener { packageManager.getLaunchIntentForPackage("com.music.streamx")?.let(::startActivity) }
        })
        setContentView(body)
    }
}

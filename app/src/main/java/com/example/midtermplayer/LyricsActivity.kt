package com.example.midtermplayer

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import com.example.midtermplayer.model.MusicModel
import kotlinx.android.synthetic.main.activity_lyrics.*

class LyricsActivity: AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_lyrics)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        val music = intent.getSerializableExtra("song") as MusicModel

        lyrics.text = application.assets.open("${music.title}.txt").bufferedReader().readText()
        lyrics.movementMethod = ScrollingMovementMethod()
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true;
    }
}
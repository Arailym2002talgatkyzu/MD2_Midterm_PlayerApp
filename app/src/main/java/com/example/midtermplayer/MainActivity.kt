package com.example.midtermplayer

import android.app.*
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.media.MediaMetadataRetriever
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.preference.PreferenceManager
import android.view.View
import android.widget.SeekBar
import android.widget.TimePicker
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.midtermplayer.adapter.MusicAdapter
import com.example.midtermplayer.model.MusicModel
import kotlinx.android.synthetic.main.activetrack.*
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*
import kotlin.collections.ArrayList

class MainActivity : AppCompatActivity() {
    private val CHANNEL_ID = "channel_id_example_01"
    private val notificationId = 101

    private lateinit var prefs: SharedPreferences
    private var music: ArrayList<MusicModel> = ArrayList()
    private var player: MediaPlayer? = null
    private var handler: Handler = Handler(Looper.getMainLooper())
    private lateinit var runnable:Runnable
    private lateinit var metadataRetriever: MediaMetadataRetriever
    private var currentSongId = -1
    private var calendar = Calendar.getInstance()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        metadataRetriever = MediaMetadataRetriever()
        createNotificationChannel()
        prefs = PreferenceManager.getDefaultSharedPreferences(this)
        getMusic()
        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onStopTrackingTouch(seekBar: SeekBar) {}
            override fun onStartTrackingTouch(seekBar: SeekBar) {}
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                if (player != null && fromUser) {
                    player!!.seekTo(progress * 1000)
                }
            }
        })
        buttonPlay.setOnClickListener {
            if (player!!.isPlaying) {
                player!!.pause()
                buttonPlay.setBackgroundResource(R.drawable.ic_play)
            } else {
                player!!.start()
                buttonPlay.setBackgroundResource(R.drawable.ic_pause)
            }
        }
        buttonPrevious.setOnClickListener {
            playPrevious()
        }
        buttonNext.setOnClickListener {
            playNext()
        }
        lyrics.setOnClickListener {
            openLyrics()
        }

        notify.setOnClickListener {
            timePicker()
        }
    }

    private fun timePicker() {
        calendar = Calendar.getInstance()
        val timePicker = TimePickerDialog.OnTimeSetListener { view: TimePicker?, hourOfDay: Int, minute: Int ->
            calendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
            calendar.set(Calendar.MINUTE, minute)
            val millis:Long = calendar.timeInMillis - System.currentTimeMillis()
            val intent: Intent = Intent(this, NotificationReceiver::class.java)
            val pendingIntent: PendingIntent = PendingIntent.getBroadcast(this, 0, intent, 0)
            val alarmManager: AlarmManager = getSystemService(ALARM_SERVICE) as AlarmManager
            val currentTime = System.currentTimeMillis()
            alarmManager.set(AlarmManager.RTC_WAKEUP, currentTime + millis, pendingIntent)
        }

        TimePickerDialog(this, timePicker, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true).show()
    }



    private fun openLyrics() {
        val intent = Intent(this, LyricsActivity::class.java).apply {
            putExtra("song", music[currentSongId])
        }
        startActivity(intent)
    }

    private fun playNext() {
        if (currentSongId != -1 && player != null) {
            player!!.stop()
            currentSongId++
            if (currentSongId >= music.size) {
                currentSongId = 0
            }
            initMusicPlayer(music[currentSongId])
            initializeSeekBar()
            player!!.start()
        }
    }

    private fun playPrevious() {
        if (currentSongId != -1 && player != null) {
            player!!.stop()
            currentSongId--
            if (currentSongId < 0) {
                currentSongId = music.size - 1
            }
            initMusicPlayer(music[currentSongId])
            initializeSeekBar()
            player!!.start()
        }
    }

    private fun getMusic() {
        val songList = listOf(
            R.raw.gurennoyumiya,
            R.raw.alga,
            R.raw.arcade,
            R.raw.lovely,
            R.raw.staywithme
        )
        for (rId in songList) {
            val mediaPath = Uri.parse("android.resource://$packageName/$rId")
            metadataRetriever.setDataSource(this, mediaPath)
            val songTitle = metadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE) ?: "none"
            val songArtist = metadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST) ?: "none"
            val album = metadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM) ?: "none"
            val model = MusicModel(resources.getResourceEntryName(rId), rId, songTitle, songArtist, album)
            music.add(model)
        }
        if (music.size > 0) {
            musicList.visibility = View.VISIBLE
            setupMusicListRecyclerView()
        } else {
            musicList.visibility = View.GONE
        }
    }

    private fun setupMusicListRecyclerView() {
        musicList.layoutManager = LinearLayoutManager(this)
        musicList.setHasFixedSize(true)

        val musicAdapter = MusicAdapter(this, music)
        musicList.adapter = musicAdapter

        musicAdapter.setOnClickListener(object : MusicAdapter.OnClickListener {
            override fun onClick(position: Int, model: MusicModel) {
                if (player != null) {
                    player!!.stop()
                }
                if (included_layout.visibility == View.GONE) {
                    included_layout.visibility = View.VISIBLE
                }
                initMusicPlayer(model)
                currentSongId = position
                initializeSeekBar()
                player!!.start()

            }
        })
    }

    private fun initMusicPlayer(model: MusicModel) {
        player = MediaPlayer.create(this@MainActivity, model.resId)
        player!!.setOnCompletionListener {
            val delay = prefs.getString("delay", "0")!!.toInt()
            if (delay != 0) {
                seekBar.max = delay
                "${formatеTimeLine(delay / 60)}:${formatеTimeLine(delay % 60)}".also { totalDuration.text = it }
                val breakTime = System.currentTimeMillis()
                runnable = Runnable {
                    val spendTime = ((System.currentTimeMillis() - breakTime) / 1000).toInt()
                    seekBar.progress = spendTime
                    " ${formatеTimeLine(spendTime / 60)}:${formatеTimeLine(spendTime % 60)}".also { currentProgress.text = it }
                    if (spendTime < delay) {
                        handler.postDelayed(runnable, 1000)
                    } else {
                        playNext()
                    }
                }

            }
        }
    }

    private fun initializeSeekBar() {
        seekBar.max = player!!.seconds
        songTitle.text = music[currentSongId].songTitle
        "${formatеTimeLine(player!!.seconds / 60)}:${
        formatеTimeLine(player!!.seconds % 60)
        }".also { totalDuration.text = it }
        runnable = Runnable {
            seekBar.progress = player!!.currentSeconds

            "${formatеTimeLine(seekBar.progress / 60)}:${
            formatеTimeLine(seekBar.progress % 60)
            }".also { currentProgress.text = it }
            handler.postDelayed(runnable, 1000)
        }
        handler.postDelayed(runnable, 1000)
    }

    private fun createNotificationChannel(){
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            val name = "Notification Title"
            val descriptionText = "Notification Description"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }

            val notificationManager: NotificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)

        }
    }

    private fun formatеTimeLine(number: Int): String {
        return number.toString().padStart(2, '0')
    }

    private val MediaPlayer.seconds:Int
        get() {
            return this.duration / 1000
        }

    private val MediaPlayer.currentSeconds:Int
        get() {
            return this.currentPosition/1000
        }


}
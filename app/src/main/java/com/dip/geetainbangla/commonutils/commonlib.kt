package com.dip.geetainbangla.commonutils

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.view.LayoutInflater
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.dip.geetainbangla.R
import com.dip.geetainbangla.model.Chapter
import com.google.gson.Gson

import android.media.MediaPlayer
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment


object commonlib {
    private var mediaPlayer: MediaPlayer? = null
    fun loadAndDisplayChapter(
        fragment: Fragment,
        jsonFileName: String,
        containerId: Int
    ) {
        var chapterNo = jsonFileName.removePrefix("chapter").removeSuffix(".json")
        val chapterBack = chapterNo
        chapterNo = if (chapterNo.toInt() > 9) "0$chapterNo" else "00$chapterNo"

        val context = fragment.requireContext()
        val jsonString = context.assets.open(jsonFileName)
            .bufferedReader()
            .use { it.readText() }

        val chapter = Gson().fromJson(jsonString, Chapter::class.java)

        val container = fragment.requireView().findViewById<LinearLayout>(containerId)
        val inflater = LayoutInflater.from(context)

        var verseNumber = 0
        for (verse in chapter.verse) {
            verseNumber++
            val view = inflater.inflate(R.layout.item_verse, container, false)

            val tvSerial = view.findViewById<TextView>(R.id.tvSerialNumber)
            val tvStanzas = view.findViewById<TextView>(R.id.tvStanzas)
            val tvMeaning = view.findViewById<TextView>(R.id.tvMeaning)

            val btnAudio = view.findViewById<ImageButton>(R.id.btnAudio)
            val btnBookmark = view.findViewById<ImageButton>(R.id.btnBookmark)
            val btnView = view.findViewById<ImageButton>(R.id.btnView)

            val verseString = if (verseNumber > 9) "0$verseNumber" else "00$verseNumber"

            btnAudio.tag = "Audio_${chapterNo}_${verseString}"
            btnBookmark.tag = "Verse_${chapterBack}_${verse.serialNumber}"
            btnView.tag = "Read_${chapterNo}_${verseString}"

            btnAudio.setOnClickListener {
                var vNum = it.tag as String
                vNum = vNum.removePrefix("Audio_")
                val audioUrl = "https://www.holy-bhagavad-gita.org/public/audio/${vNum}.mp3"
                Log.d("AudioPlayer", "Audio URL: $audioUrl")
                (fragment.requireActivity() as? AppCompatActivity)?.playAudioFromUrl(audioUrl)
            }

            btnBookmark.setOnClickListener {
                val vNum = it.tag as String
                // handle bookmark logic
            }

            btnView.setOnClickListener {
                val vNum = it.tag as String
                // handle view logic
            }

            tvSerial.text = "${context.getString(R.string.verse_number)} ${verse.serialNumber}"
            tvStanzas.text = verse.stanzas.joinToString("\n")
            tvMeaning.text = "${context.getString(R.string.verse_meaning)}\n${verse.meaning}"

            container.addView(view)
        }
    }


    fun AppCompatActivity.playAudioFromUrl(url: String) {
        try {
            mediaPlayer?.release() // release any existing player
            mediaPlayer = MediaPlayer().apply {
                setDataSource(url)
                setOnPreparedListener {
                    it.start()
                }
                setOnCompletionListener {
                    // optionally release resources after playback
                    it.release()
                    mediaPlayer = null
                }
                setOnErrorListener { mp, what, extra ->
                    Log.e("AudioPlayer", "Error playing audio: $what $extra")
                    mp.release()
                    mediaPlayer = null
                    true
                }
                prepareAsync() // non-blocking
            }
        } catch (e: Exception) {
            e.printStackTrace()
            //showAudioUrlDialog(this@playAudioFromUrl,url)
            Toast.makeText(this@playAudioFromUrl, "Failed to play audio!", Toast.LENGTH_SHORT).show()
        }
    }

fun showAudioUrlDialog(context: Context, audioUrl: String) {
    AlertDialog.Builder(context)
        .setTitle("Audio URL")
        .setMessage(audioUrl)
        .setPositiveButton("Copy") { dialog, _ ->
            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText("Audio URL", audioUrl)
            clipboard.setPrimaryClip(clip)
            Toast.makeText(context, "URL copied to clipboard!", Toast.LENGTH_SHORT).show()
            dialog.dismiss()
        }
        .setNegativeButton("Play Audio") { dialog, _ ->
            if (context is AppCompatActivity) {
                context.playAudioFromUrl(audioUrl)
            } else {
                Toast.makeText(context, "Cannot play audio in this context.", Toast.LENGTH_SHORT).show()
            }
            dialog.dismiss()
        }
        .setNeutralButton("Cancel", null)
        .show()
}

fun loadAndDisplayIntroduction(
    fragment: Fragment,
    jsonFileName: String,
    containerId: Int
) {

    val context = fragment.requireContext()
    val jsonString = context.assets.open(jsonFileName)
        .bufferedReader()
        .use { it.readText() }

    val chapter = Gson().fromJson(jsonString, Chapter::class.java)

    val container = fragment.requireView().findViewById<LinearLayout>(containerId)
    val inflater = LayoutInflater.from(context)

    var verseNumber = 0
    for (verse in chapter.verse) {
        verseNumber++
        val view = inflater.inflate(R.layout.item_introverse, container, false)

        val tvSerial = view.findViewById<TextView>(R.id.tvSerialNumber)
        val tvStanzas = view.findViewById<TextView>(R.id.tvStanzas)
        val tvMeaning = view.findViewById<TextView>(R.id.tvMeaning)

        tvSerial.text = "${context.getString(R.string.verse_number)} ${verse.serialNumber}"
        tvStanzas.text = verse.stanzas.joinToString("\n")
        tvMeaning.text = "${context.getString(R.string.verse_meaning)}\n${verse.meaning}"

        container.addView(view)
    }
}

}


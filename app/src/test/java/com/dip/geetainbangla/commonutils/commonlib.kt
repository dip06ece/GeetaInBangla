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

object commonlib {
    private var mediaPlayer: MediaPlayer? = null
    fun loadAndDisplayChapter(
        activity: AppCompatActivity,
        jsonFileName: String,
        containerId: Int
    ) {
        // Extracting chapter number from json file name for dynamic ID
        var chapterNo = jsonFileName.removePrefix("chapter").removeSuffix(".json")
        var chapterBack = chapterNo
        if (chapterNo.toInt()>9){
            chapterNo = "0$chapterNo"
        }else{
            chapterNo = "00$chapterNo"
        }
        // Chapter number stored in chapterNo

        val jsonString = activity.assets.open(jsonFileName)
            .bufferedReader()
            .use { it.readText() }

        val chapter = Gson().fromJson(jsonString, Chapter::class.java)

        val container = activity.findViewById<LinearLayout>(containerId)

        val inflater = LayoutInflater.from(activity)

        var verseNumber = 0 // Starting from 0
        for (verse in chapter.verse) {
            verseNumber++  // for every verse increment by 1
            val view = inflater.inflate(R.layout.item_verse, container, false)

            val tvSerial = view.findViewById<TextView>(R.id.tvSerialNumber)
            val tvStanzas = view.findViewById<TextView>(R.id.tvStanzas)
            val tvMeaning = view.findViewById<TextView>(R.id.tvMeaning)
            // ********************************************************
            val btnAudio = view.findViewById<ImageButton>(R.id.btnAudio)
            val btnBookmark = view.findViewById<ImageButton>(R.id.btnBookmark)
            //val btnView = view.findViewById<ImageButton>(R.id.btnView)
            // Calculate verseString to three Digits
            var verseString = ""
            if(verseNumber>9){
                verseString = "0${verseNumber}"
            }else{
                verseString = "00${verseNumber}"
            }
            //
            btnAudio.tag = "Audio_${chapterNo}_${verseString}"
            btnBookmark.tag = "Verse_${chapterBack}_${verse.serialNumber}"
            //btnView.tag = "Read_${chapterNo}_${verseString}"
            // OPTIONAL: example click listeners
            btnAudio.setOnClickListener {
                var vNum = it.tag as String
                vNum = vNum.removePrefix("Audio_")
                val audioUrl = "https://www.holy-bhagavad-gita.org/public/audio/${vNum}.mp3"
                Log.d("AudioPlayer", "Audio URL: $audioUrl")
                (it.context as? AppCompatActivity)?.playAudioFromUrl(audioUrl)
            }

            btnBookmark.setOnClickListener {
                val vNum = it.tag as String
                //Toast.makeText(it.context, "$vNum", Toast.LENGTH_LONG).show()
            }

//            btnView.setOnClickListener {
//                val vNum = it.tag as String
//                // read processing
//            }
            // ****************************************
            val verseLabel = activity.getString(R.string.verse_number)
            tvSerial.text = "$verseLabel ${verse.serialNumber}"
            tvStanzas.text = verse.stanzas.joinToString("\n")
            val verseMeaning = activity.getString(R.string.verse_meaning)
            tvMeaning.text = "$verseMeaning \n ${verse.meaning}"

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
            Toast.makeText(this@playAudioFromUrl, "Failed to play audio.", Toast.LENGTH_SHORT).show()
        }
    }

}
//Toast.makeText(it.context, "Audio URL:\n$audioUrl", Toast.LENGTH_LONG).show()
//                val context = it.context

//                AlertDialog.Builder(context)
//                    .setTitle("Audio URL")
//                    .setMessage(audioUrl)
//                    .setPositiveButton("Copy") { dialog, _ ->
//                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
//                        val clip = ClipData.newPlainText("Audio URL", audioUrl)
//                        clipboard.setPrimaryClip(clip)
//                        Toast.makeText(context, "URL copied to clipboard!", Toast.LENGTH_SHORT).show()
//                        dialog.dismiss()
//                    }
//                    .setNegativeButton("Play Audio") { dialog, _ ->
//                        playAudioFromUrl(audioUrl)
//                        dialog.dismiss()
//                    }
//                    .setNeutralButton("Cancel", null)
//                    .show()
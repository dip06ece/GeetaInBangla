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
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import com.dip.geetainbangla.model.Bookmark
import com.dip.geetainbangla.model.Bookmarks
import java.io.File


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
            //val btnView = view.findViewById<ImageButton>(R.id.btnView)

            val verseString = if (verseNumber > 9) "0$verseNumber" else "00$verseNumber"

            btnAudio.tag = "Audio_${chapterNo}_${verseString}"
            btnBookmark.tag = "Verse_${chapterNo}_${verseString}_${verse.serialNumber}"
                    //"${chapterBack}_${verse.serialNumber}"
            //btnView.tag = "Read_${chapterNo}_${verseString}"

            btnAudio.setOnClickListener {
                var vNum = it.tag as String
                vNum = vNum.removePrefix("Audio_")
                //Toast.makeText(context, vNum, Toast.LENGTH_SHORT).show()
                //val audioUrl = "https://www.holy-bhagavad-gita.org/public/audio/${vNum}.mp3"
                //val audioUrl = "https://www.holy-bhagavad-gita.org/media/audios/${vNum}.mp3"
                val audioUrl = verse.site_url
                Log.d("AudioPlayer", "Audio URL: $audioUrl")
                (fragment.requireActivity() as? AppCompatActivity)?.playAudioFromUrl(audioUrl)
            }

            btnBookmark.setOnClickListener {
                var vNum = it.tag as String
                //Toast.makeText(context, vNum, Toast.LENGTH_SHORT).show()
                vNum = vNum.removePrefix("Verse_")
                val parts = vNum.split("_")
                val chapter = parts[0].toInt()
                val chapterText = this.convertChapterNumberToBangla(chapter)
                val serialNumber = parts[1]
                val serialNumberBangla = parts[2]
                val context = fragment.requireContext()
                val file = File(context.filesDir, "bookmarks.json")
                val bookmarksJson = if (file.exists()) {
                    file.readText()
                } else {
                    """{"bookmark": []}"""
                }
                val bookmarks = Gson().fromJson(bookmarksJson, Bookmarks::class.java)
                val alreadyExists = bookmarks.bookmark.any {
                    it.key == vNum
                }
                if (alreadyExists) {
                    Toast.makeText(context, "Already bookmarked!", Toast.LENGTH_SHORT).show()
                } else {
                    val newBookmark = Bookmark(
                        key = vNum,
                        chapter = chapterText,
                        serialNumber = serialNumberBangla,
                        stanzas = verse.stanzas,
                        meaning = verse.meaning
                    )

                    val updatedList = bookmarks.bookmark.toMutableList()
                    updatedList.add(newBookmark)

                    val updatedBookmarks = Bookmarks(updatedList)
                    val updatedJson = Gson().toJson(updatedBookmarks)

                    file.writeText(updatedJson)

                    Toast.makeText(context, "Bookmark saved!", Toast.LENGTH_SHORT).show()
                }
            }

//            btnView.setOnClickListener {
//                val vNum = it.tag as String
//                // handle view logic
//            }

            tvSerial.text = "${context.getString(R.string.verse_number)} ${verse.serialNumber}"
            tvStanzas.text = verse.stanzas.joinToString("\n")
            tvMeaning.text = "${context.getString(R.string.verse_meaning)}\n${verse.meaning}"

            container.addView(view)
        }
    }

    fun AppCompatActivity.playAudioFromUrl(url: String) {
        try {
            Toast.makeText(this@playAudioFromUrl, "অডিও লোড হচ্ছে...", Toast.LENGTH_SHORT).show()

            mediaPlayer?.release()

            val handler = Handler(Looper.getMainLooper())
            var timeoutRunnable: Runnable? = null

            mediaPlayer = MediaPlayer().apply {
                setDataSource(url)

                setOnPreparedListener {
                    // Cancel timeout if prepared successfully
                    timeoutRunnable?.let { handler.removeCallbacks(it) }
                    it.start()
                }

                setOnCompletionListener {
                    it.release()
                    mediaPlayer = null
                }

                setOnErrorListener { mp, what, extra ->
                    timeoutRunnable?.let { handler.removeCallbacks(it) }
                    Toast.makeText(
                        this@playAudioFromUrl,
                        "অডিও ফাইলটি পাওয়া যায় নি",
                        Toast.LENGTH_SHORT
                    ).show()
                    Log.e("AudioPlayer", "Error playing audio: $what $extra")
                    mp.release()
                    mediaPlayer = null
                    true
                }

                prepareAsync()

                // start timeout countdown
                timeoutRunnable = Runnable {
                    release()
                    mediaPlayer = null
                    Toast.makeText(
                        this@playAudioFromUrl,
                        "অডিও ফাইলটি চালু করা যায় নি",
                        Toast.LENGTH_LONG
                    ).show()
                }
                handler.postDelayed(timeoutRunnable!!, 10000) // 10 seconds timeout
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(
                this@playAudioFromUrl,
                "অডিও লোড করতে সমস্যা হচ্ছে। অনুগ্রহ করে ইন্টারনেট সংযোগ পরীক্ষা করুন।",
                Toast.LENGTH_LONG
            ).show()
        }
    }

//fun AppCompatActivity.playAudioFromUrl(url: String) {
//    try {
//        mediaPlayer?.release() // release any existing player
//        mediaPlayer = MediaPlayer().apply {
//            setDataSource(url)
//            setOnPreparedListener {
//                it.start()
//            }
//            setOnCompletionListener {
//                // optionally release resources after playback
//                it.release()
//                mediaPlayer = null
//            }
//            setOnErrorListener { mp, what, extra ->
//                Toast.makeText(this@playAudioFromUrl, "এই অডিও ফাইলটি পাওয়া যায় নি", Toast.LENGTH_SHORT).show()
//                Log.e("AudioPlayer", "Error playing audio: $what $extra")
//                mp.release()
//                mediaPlayer = null
//                true
//            }
//            prepareAsync() // non-blocking
//        }
//    } catch (e: Exception) {
//        e.printStackTrace()
//        //showAudioUrlDialog(this@playAudioFromUrl,url)
//        Toast.makeText(this@playAudioFromUrl, "অডিও ফাইলটি চালু করা যায় নি", Toast.LENGTH_SHORT).show()
//    }
//}

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

fun loadAndDisplayBookmark(
    fragment: Fragment,
    jsonFileName: String,
    containerId: Int
) {

    val context = fragment.requireContext()
    val file = File(context.filesDir, jsonFileName)
    val jsonString = if (file.exists()) {
        file.readText()
    } else {
        """{"bookmark": []}"""
    }
//    jsonString = context.assets.open(jsonFileName)
//        .bufferedReader()
//        .use { it.readText() }

    val bookmarks = Gson().fromJson(jsonString, Bookmarks::class.java)

    val container = fragment.requireView().findViewById<LinearLayout>(containerId)
    val inflater = LayoutInflater.from(context)

    var bookmarkNumber = 0
    for (bookmark in bookmarks.bookmark) {
        bookmarkNumber++
        val view = inflater.inflate(R.layout.bookmark, container, false)

        val tvChapter = view.findViewById<TextView>(R.id.tvChapter)
        val tvSerial = view.findViewById<TextView>(R.id.tvSerialNumber)
        val tvStanzas = view.findViewById<TextView>(R.id.tvStanzas)
        val tvMeaning = view.findViewById<TextView>(R.id.tvMeaning)


        tvChapter.text = "${context.getString(R.string.chapter_number)} ${convertChapterNumberToBangla(bookmark.chapter.toInt())}"
        tvSerial.text = "${context.getString(R.string.verse_number)} ${bookmark.serialNumber}"
        tvStanzas.text = bookmark.stanzas.joinToString("\n")
        tvMeaning.text = "${context.getString(R.string.verse_meaning)}\n${bookmark.meaning}"

        val btnAudio = view.findViewById<ImageButton>(R.id.btnAudio)
        val btnBookmark = view.findViewById<ImageButton>(R.id.btnBookmark)

        val verseString = bookmark.key

        btnAudio.tag = "Audio_${verseString}"
        btnBookmark.tag = "Verse_${verseString}"

        btnAudio.setOnClickListener {   // need to fix the key
            var vNum = it.tag as String
            vNum = vNum.removePrefix("Audio_")
            val parts = vNum.split("_")
            val tag_name = "${parts[0]}_${parts[1]}"
            //showAudioUrlDialog(context,vNum)
            val audioUrl = "https://www.holy-bhagavad-gita.org/media/audios/${tag_name}.mp3"
            Log.d("AudioPlayer", "Audio URL: $audioUrl")
            (fragment.requireActivity() as? AppCompatActivity)?.playAudioFromUrl(audioUrl)
        }
        btnBookmark.setOnClickListener {
            var vNum = it.tag as String
            vNum = vNum.removePrefix("Verse_")

            val context = fragment.requireContext()
            val file = File(context.filesDir, "bookmarks.json")
            val bookmarksJson = if (file.exists()) {
                file.readText()
            } else {
                """{"bookmark": []}"""
            }

            val bookmarks = Gson().fromJson(bookmarksJson, Bookmarks::class.java)
            val updatedList = bookmarks.bookmark.toMutableList()
            val existingBookmark = updatedList.find { it.key == vNum }
            if (existingBookmark != null) {
                updatedList.remove(existingBookmark)
                val updatedBookmarks = Bookmarks(updatedList)
                val updatedJson = Gson().toJson(updatedBookmarks)
                file.writeText(updatedJson)
                Toast.makeText(context, "Bookmark removed!", Toast.LENGTH_SHORT).show()
                // Clear the container first
                val container = fragment.requireView().findViewById<LinearLayout>(R.id.bookmarkContainer)
                container.removeAllViews()

                // Reload bookmarks
                commonlib.loadAndDisplayBookmark(
                    fragment = fragment,
                    jsonFileName = "bookmarks.json",
                    containerId = R.id.bookmarkContainer
                )
            }
        }
        container.addView(view)
    }
}

fun convertChapterNumberToBangla(chapter: Int): String {
    // Digits mapping: 0-9 to Bangla numerals
    val banglaDigits = arrayOf("০", "১", "২", "৩", "৪", "৫", "৬", "৭", "৮", "৯", "১০", "১১", "১২", "১৩", "১৪", "১৫", "১৬", "১৭", "১৮")
    return if (chapter in 1..18) banglaDigits[chapter] else ""
}

}


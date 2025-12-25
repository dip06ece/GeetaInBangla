package com.dip.geetainbangla.commonutils

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.PorterDuff
import android.media.AudioAttributes
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
import androidx.core.content.ContextCompat
import androidx.core.widget.ImageViewCompat
import androidx.fragment.app.Fragment
import com.dip.geetainbangla.model.Bookmark
import com.dip.geetainbangla.model.Bookmarks
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.net.URL


object commonlib {
    private var mediaPlayer: MediaPlayer? = null

    fun getCachedAudioFileNames(context: Context): List<String> {
        val audioDir = File(context.filesDir, "audio")

        if (!audioDir.exists()) {
            return emptyList()
        }

        return audioDir.listFiles()
            ?.filter { it.isFile && it.extension == "mp3" }
            ?.map { it.name }   // only file names
            ?: emptyList()
    }
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
        val cachedAudioFiles: List<String> = getCachedAudioFileNames(context)
        var verseNumber = 0
        for (verse in chapter.verse) {
            verseNumber++
            val view = inflater.inflate(R.layout.item_verse, container, false)

            val btnView = view.findViewById<ImageButton>(R.id.btnView)
            val verse_store_url = verse.store_url
            if (cachedAudioFiles.contains(verse_store_url)) {
                // Audio already downloaded → eye bright (gray)
                //btnView.setImageResource(R.drawable.ic_eye_open)
                ImageViewCompat.setImageTintList(
                    btnView,
                    ColorStateList.valueOf(
                        ContextCompat.getColor(context, android.R.color.darker_gray)
                    )
                )
            } else {
                // Not downloaded → eye black (black)
                //btnView.setImageResource(R.drawable.ic_eye_closed)
                ImageViewCompat.setImageTintList(
                    btnView,
                    ColorStateList.valueOf(
                        ContextCompat.getColor(context, android.R.color.black)
                    )
                )
            }

            val tvSerial = view.findViewById<TextView>(R.id.tvSerialNumber)
            val tvStanzas = view.findViewById<TextView>(R.id.tvStanzas)
            val tvMeaning = view.findViewById<TextView>(R.id.tvMeaning)

            val btnAudio = view.findViewById<ImageButton>(R.id.btnAudio)
            val btnBookmark = view.findViewById<ImageButton>(R.id.btnBookmark)

            val verseString = if (verseNumber > 9) "0$verseNumber" else "00$verseNumber"

            btnAudio.tag = "Audio_${chapterNo}_${verseString}"
            btnBookmark.tag = "Verse_${chapterNo}_${verseString}_${verse.serialNumber}"

            btnAudio.setOnClickListener {
                val audioUrl = verse.site_url
                val filename = verse.store_url
                Log.d("AudioPlayer", "Audio URL: $audioUrl")
                (fragment.requireActivity() as? AppCompatActivity)?.playAudioFromUrl(audioUrl,filename,) {
                    // ▶ INSTANT EYE UPDATE
                    ImageViewCompat.setImageTintList(
                        btnView,
                        ColorStateList.valueOf(
                            ContextCompat.getColor(context, android.R.color.darker_gray)
                        )
                    )
                }
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
                        meaning = verse.meaning,
                        id = verse.id,
                        site_url = verse.site_url,
                        store_url = verse.store_url
                    )

                    val updatedList = bookmarks.bookmark.toMutableList()
                    updatedList.add(newBookmark)

                    val updatedBookmarks = Bookmarks(updatedList)
                    val updatedJson = Gson().toJson(updatedBookmarks)

                    file.writeText(updatedJson)

                    Toast.makeText(context, "Bookmark saved!", Toast.LENGTH_SHORT).show()
                }
            }

            btnView.setOnClickListener {
                stopAudio(context)
            }

            tvSerial.text = "${context.getString(R.string.verse_number)} ${verse.serialNumber}"
            tvStanzas.text = verse.stanzas.joinToString("\n")
            tvMeaning.text = "${context.getString(R.string.verse_meaning)}\n${verse.meaning}"

            container.addView(view)
        }
    }

    fun stopAudio(context: Context) {
        try {
            mediaPlayer?.let {
                if (it.isPlaying) {
                    it.stop()
                }
                it.release()
            }
            mediaPlayer = null
            Toast.makeText(context, "অডিও বন্ধ করা হয়েছে", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    fun AppCompatActivity.playAudioFromUrl(primaryUrl: String, localFileName: String, onDownloaded: (() -> Unit)? = null) {
        val audioDir = File(filesDir, "audio").apply {
            if (!exists()) mkdirs()
        }

        val localFile = File(audioDir, localFileName)

        fun playFromFile(file: File) {
            try {
                mediaPlayer?.release()
                Toast.makeText(this, "পূর্বে ডাঊনলোড করা ফাইল থেকে চালানোর চেষ্টা করা হচ্ছে...", Toast.LENGTH_SHORT).show()
                mediaPlayer = MediaPlayer().apply {
                    setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_MEDIA)
                            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                            .build()
                    )
                    setDataSource(file.absolutePath)
                    setOnPreparedListener { it.start() }
                    setOnCompletionListener {
                        it.release()
                        mediaPlayer = null
                    }
                    setOnErrorListener { mp, _, _ ->
                        mp.release()
                        mediaPlayer = null
                        Toast.makeText(
                            this@playAudioFromUrl,
                            "অডিও চালানো যায়নি",
                            Toast.LENGTH_LONG
                        ).show()
                        true
                    }
                    prepareAsync()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        fun downloadAndPlay() {
            Toast.makeText(this, "অডিও ডাউনলোড হচ্ছে...", Toast.LENGTH_SHORT).show()

            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val url = URL(primaryUrl)
                    val connection = url.openConnection()
                    connection.connect()

                    connection.getInputStream().use { input ->
                        FileOutputStream(localFile).use { output ->
                            input.copyTo(output)
                        }
                    }

                    withContext(Dispatchers.Main) {
                        // PLAY AUDIO
                        playFromFile(localFile)

                        // UPDATE UI (eye open)
                        onDownloaded?.invoke()

                    }

                } catch (e: Exception) {
                    e.printStackTrace()
                    withContext(Dispatchers.Main) {
                        Toast.makeText(
                            this@playAudioFromUrl,
                            "অডিও ডাউনলোড করা যায়নি",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            }
        }

        // ▶ OFFLINE-FIRST LOGIC
        if (localFile.exists()) {
            playFromFile(localFile)
        } else {
            downloadAndPlay()
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
                context.playAudioFromUrl(audioUrl, audioUrl)// ==>>>>>>>>>
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

    val bookmarksData = Gson().fromJson(jsonString, Bookmarks::class.java)
    val container = fragment.requireView().findViewById<LinearLayout>(containerId)
    val inflater = LayoutInflater.from(context)

    container.removeAllViews()

    // 🔹 GROUP BY CHAPTER (numeric)
    val groupedByChapter = bookmarksData.bookmark
        .groupBy { it.chapter.toInt() }
        .toSortedMap() // sort chapters ASC

    for ((chapterNumber, chapterBookmarks) in groupedByChapter) {

        // 🔹 Add chapter header
        val chapterHeader = TextView(context).apply {
            text = "অধ্যায় ${convertChapterNumberToBangla(chapterNumber)}"
            textSize = 18f
            setPadding(16, 24, 16, 8)
            setTextColor(ContextCompat.getColor(context, R.color.black))
        }
        container.addView(chapterHeader)

        // 🔹 Sort verses inside chapter by id
        val sortedBookmarks = chapterBookmarks.sortedBy { it.id.toInt() }

        for (bookmark in sortedBookmarks) {

            val view = inflater.inflate(R.layout.bookmark, container, false)

            val tvChapter = view.findViewById<TextView>(R.id.tvChapter)
            val tvSerial = view.findViewById<TextView>(R.id.tvSerialNumber)
            val tvStanzas = view.findViewById<TextView>(R.id.tvStanzas)
            val tvMeaning = view.findViewById<TextView>(R.id.tvMeaning)

            tvChapter.text =
                "${context.getString(R.string.chapter_number)} ${convertChapterNumberToBangla(bookmark.chapter.toInt())}"
            tvSerial.text =
                "${context.getString(R.string.verse_number)} ${bookmark.serialNumber}"
            tvStanzas.text = bookmark.stanzas.joinToString("\n")
            tvMeaning.text =
                "${context.getString(R.string.verse_meaning)}\n${bookmark.meaning}"

            val btnAudio = view.findViewById<ImageButton>(R.id.btnAudio)
            val btnBookmark = view.findViewById<ImageButton>(R.id.btnBookmark)

            btnAudio.setOnClickListener {
                (fragment.requireActivity() as? AppCompatActivity)
                    ?.playAudioFromUrl(bookmark.site_url, bookmark.store_url)
            }

            btnBookmark.setOnClickListener {
                removeBookmarkAndReload(
                    fragment = fragment,
                    key = bookmark.key,
                    containerId = containerId
                )
            }

            container.addView(view)
        }
    }
}

private fun removeBookmarkAndReload(
    fragment: Fragment,
    key: String,
    containerId: Int
) {
    val context = fragment.requireContext()
    val file = File(context.filesDir, "bookmarks.json")

    val jsonString = file.readText()
    val bookmarks = Gson().fromJson(jsonString, Bookmarks::class.java)

    val updatedList = bookmarks.bookmark.filterNot { it.key == key }
    file.writeText(Gson().toJson(Bookmarks(updatedList)))

    Toast.makeText(context, "Bookmark removed!", Toast.LENGTH_SHORT).show()

    commonlib.loadAndDisplayBookmark(
        fragment = fragment,
        jsonFileName = "bookmarks.json",
        containerId = containerId
    )
}

    fun convertChapterNumberToBangla(chapter: Int): String {
    // Digits mapping: 0-9 to Bangla numerals
    val banglaDigits = arrayOf("০", "১", "২", "৩", "৪", "৫", "৬", "৭", "৮", "৯", "১০", "১১", "১২", "১৩", "১৪", "১৫", "১৬", "১৭", "১৮")
    return if (chapter in 1..18) banglaDigits[chapter] else ""
}

}


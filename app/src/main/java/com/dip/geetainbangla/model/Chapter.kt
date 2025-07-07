// app/src/main/java/com/dip/banglageeta/model/Chapter.kt

package com.dip.geetainbangla.model

data class Chapter(
    val verse: List<Verse>
)

data class Verse(
    val serialNumber: String,
    val stanzas: List<String>,
    val meaning: String
)

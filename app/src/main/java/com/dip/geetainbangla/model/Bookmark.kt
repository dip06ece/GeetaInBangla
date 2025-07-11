package com.dip.geetainbangla.model

data class Bookmarks (
    val bookmark: List<Bookmark>
)

data class Bookmark(
    val key: String,
    val chapter: String,
    val serialNumber: String,
    val stanzas: List<String>,
    val meaning: String
)
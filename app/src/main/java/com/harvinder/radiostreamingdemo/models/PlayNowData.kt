package com.harvinder.radiostreamingdemo.models

import androidx.room.Entity
import androidx.room.PrimaryKey

class GetPlayNowData : ArrayList<GetPlayNowDataItem>()
@Entity(tableName = "users_table")
data class GetPlayNowDataItem(
    val album: String,
    val artist: String,
    val image_url: String,
    val link_url: String,
    @PrimaryKey(autoGenerate = false)
    val name: String,
    val played_at: String,
    val preview_url: String,
    val sid: String
)


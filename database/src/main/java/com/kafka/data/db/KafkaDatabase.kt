package com.kafka.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.kafka.data.dao.AuthDao
import com.kafka.data.dao.DownloadRequestsDao
import com.kafka.data.dao.FileDao
import com.kafka.data.dao.ItemDao
import com.kafka.data.dao.ItemDetailDao
import com.kafka.data.dao.RecentAudioDao
import com.kafka.data.dao.RecentSearchDao
import com.kafka.data.dao.RecentTextDao
import com.kafka.data.dao.SearchDao
import com.kafka.data.entities.DownloadRequest
import com.kafka.data.entities.File
import com.kafka.data.entities.Item
import com.kafka.data.entities.ItemDetail
import com.kafka.data.entities.QueueEntity
import com.kafka.data.entities.RecentAudioItem
import com.kafka.data.entities.RecentSearch
import com.kafka.data.entities.RecentTextItem
import com.kafka.data.entities.User

interface KafkaDatabase {
    fun itemDetailDao(): ItemDetailDao
    fun fileDao(): FileDao
    fun searchDao(): SearchDao
    fun itemDao(): ItemDao
    fun recentSearchDao(): RecentSearchDao
    fun recentTextDao(): RecentTextDao
    fun recentAudioDao(): RecentAudioDao
    fun downloadRequestsDao(): DownloadRequestsDao
    fun authDao(): AuthDao
}

@Database(
    entities = [
        ItemDetail::class,
        File::class,
        Item::class,
        QueueEntity::class,
        RecentSearch::class,
        RecentTextItem::class,
        RecentAudioItem::class,
        DownloadRequest::class,
        User::class
    ],
    version = 2,
    exportSchema = false
)
@TypeConverters(AppTypeConverters::class)
abstract class KafkaRoomDatabase : RoomDatabase(), KafkaDatabase

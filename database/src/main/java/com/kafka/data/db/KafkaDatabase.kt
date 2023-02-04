package com.kafka.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.kafka.data.dao.DownloadRequestsDao
import com.kafka.data.dao.FileDao
import com.kafka.data.dao.FollowedItemDao
import com.kafka.data.dao.ItemDao
import com.kafka.data.dao.ItemDetailDao
import com.kafka.data.dao.RecentItemDao
import com.kafka.data.dao.RecentSearchDao
import com.kafka.data.dao.SearchDao
import com.kafka.data.dao.TextFileDao
import com.kafka.data.entities.DownloadRequest
import com.kafka.data.entities.File
import com.kafka.data.entities.FollowedItem
import com.kafka.data.entities.Item
import com.kafka.data.entities.ItemDetail
import com.kafka.data.entities.QueueEntity
import com.kafka.data.entities.RecentItem
import com.kafka.data.entities.RecentSearch
import com.kafka.data.entities.TextFile

interface KafkaDatabase {
    fun itemDetailDao(): ItemDetailDao
    fun fileDao(): FileDao
    fun recentItemDao(): RecentItemDao
    fun followedItemDao(): FollowedItemDao
    fun searchDao(): SearchDao
    fun itemDao(): ItemDao
    fun textFileDao(): TextFileDao
    fun recentSearchDao(): RecentSearchDao
    fun downloadRequestsDao(): DownloadRequestsDao
}

@Database(
    entities = [
        ItemDetail::class,
        File::class,
        Item::class,
        TextFile::class,
        RecentItem::class,
        FollowedItem::class,
        QueueEntity::class,
        RecentSearch::class,
        DownloadRequest::class
    ],
    version = 2,
    exportSchema = false
)
@TypeConverters(AppTypeConverters::class)
abstract class KafkaRoomDatabase : RoomDatabase(), KafkaDatabase

package com.example.blogposts.persistence

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.blogposts.models.AccountProperties
import com.example.blogposts.models.AuthToken
import com.example.blogposts.models.BlogPost

@Database(entities = [AccountProperties::class, AuthToken::class, BlogPost::class], version = 1)
abstract class AppDatabase: RoomDatabase() {

    abstract fun getAuthTokenDao(): AuthTokenDao

    abstract fun getAccountPropertiesDao(): AccountPropertiesDao

    abstract fun getBlogPostDao(): BlogPostDao

    companion object{

        const val DATABASE_NAME = "app_db"
    }

}
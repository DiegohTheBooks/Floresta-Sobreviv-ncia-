package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.model.CampStructureEntity
import com.example.data.model.ChestItemEntity
import com.example.data.model.InventoryItemEntity
import com.example.data.model.JournalEntryEntity
import com.example.data.model.SurvivalProfile

@Database(
  entities = [
    SurvivalProfile::class,
    InventoryItemEntity::class,
    CampStructureEntity::class,
    ChestItemEntity::class,
    JournalEntryEntity::class
  ],
  version = 1,
  exportSchema = false
)
abstract class SurvivalDatabase : RoomDatabase() {
  abstract fun survivalDao(): SurvivalDao

  companion object {
    @Volatile
    private var INSTANCE: SurvivalDatabase? = null

    fun getDatabase(context: Context): SurvivalDatabase {
      return INSTANCE ?: synchronized(this) {
        val instance = Room.databaseBuilder(
          context.applicationContext,
          SurvivalDatabase::class.java,
          "floresta_survival_db"
        )
          .fallbackToDestructiveMigration()
          .build()
        INSTANCE = instance
        instance
      }
    }
  }
}

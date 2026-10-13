package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.local.dao.ActivityMasterDao
import com.example.data.local.dao.ActivityReportDao
import com.example.data.local.dao.DraftRealisasiDao
import com.example.data.local.dao.HasilInputAktivitasDao
import com.example.data.local.dao.KendalaDao
import com.example.data.local.dao.LokasiDao
import com.example.data.local.dao.OperatorDao
import com.example.data.local.dao.RencanaKerjaDao
import com.example.data.local.dao.AktivitasUnitDao
import com.example.data.local.dao.UnitDao
import com.example.data.local.dao.UserDao
import com.example.data.local.entity.ActivityMasterEntity
import com.example.data.local.entity.ActivityReportEntity
import com.example.data.local.entity.AktivitasUnitEntity
import com.example.data.local.entity.DraftRealisasiEntity
import com.example.data.local.entity.HasilInputAktivitasEntity
import com.example.data.local.entity.KendalaEntity
import com.example.data.local.entity.LokasiEntity
import com.example.data.local.entity.OperatorEntity
import com.example.data.local.entity.RencanaKerjaEntity
import com.example.data.local.entity.UnitEntity
import com.example.data.local.entity.UserEntity

@Database(
    entities = [
        UnitEntity::class,
        ActivityMasterEntity::class,
        OperatorEntity::class,
        ActivityReportEntity::class,
        LokasiEntity::class,
        RencanaKerjaEntity::class,
        HasilInputAktivitasEntity::class,
        KendalaEntity::class,
        DraftRealisasiEntity::class,
        AktivitasUnitEntity::class,
        UserEntity::class
    ],
    version = 19,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun unitDao(): UnitDao
    abstract fun activityMasterDao(): ActivityMasterDao
    abstract fun operatorDao(): OperatorDao
    abstract fun activityReportDao(): ActivityReportDao
    abstract fun lokasiDao(): LokasiDao
    abstract fun rencanaKerjaDao(): RencanaKerjaDao
    abstract fun hasilInputAktivitasDao(): HasilInputAktivitasDao
    abstract fun kendalaDao(): KendalaDao
    abstract fun draftRealisasiDao(): DraftRealisasiDao
    abstract fun aktivitasUnitDao(): AktivitasUnitDao
    abstract fun userDao(): UserDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "heavytrack_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}

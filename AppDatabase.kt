package com.example.mediassistant.data

import android.content.Context
import androidx.room.*

@Dao
interface PatientDao {
    @Query("SELECT * FROM patients ORDER BY createdAt DESC")
    suspend fun getAllPatients(): List<Patient>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPatient(patient: Patient): Long

    @Update
    suspend fun updatePatient(patient: Patient)

    @Delete
    suspend fun deletePatient(patient: Patient)

    @Query("SELECT * FROM patients WHERE id = :id")
    suspend fun getPatientById(id: Int): Patient?
}

@Database(entities = [Patient::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun patientDao(): PatientDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "mediassistant.db"
                ).build().also { INSTANCE = it }
            }
    }
}

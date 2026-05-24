package gamalsolutions.gamalprojects.autosavecontactscrm.database

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface CRMDao {
    
    // Contacts Queries
    @Query("SELECT * FROM contacts ORDER BY lastInteractionTimestamp DESC")
    fun getAllContactsFlow(): Flow<List<ContactEntity>>
    
    @Query("SELECT * FROM contacts ORDER BY lastInteractionTimestamp DESC")
    suspend fun getAllContacts(): List<ContactEntity>

    @Query("SELECT * FROM contacts WHERE phoneNumber = :phone LIMIT 1")
    suspend fun getContactByPhone(phone: String): ContactEntity?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertContact(contact: ContactEntity): Long
    
    @Update
    suspend fun updateContact(contact: ContactEntity)
    
    @Delete
    suspend fun deleteContact(contact: ContactEntity)
    
    // Logs Queries
    @Query("SELECT * FROM operation_logs ORDER BY timestamp DESC")
    fun getAllLogsFlow(): Flow<List<LogEntity>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: LogEntity): Long
    
    @Query("DELETE FROM operation_logs")
    suspend fun clearAllLogs()

    // Settings Queries
    @Query("SELECT * FROM settings WHERE `key` = :key LIMIT 1")
    suspend fun getSetting(key: String): SettingEntity?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSetting(setting: SettingEntity)
}

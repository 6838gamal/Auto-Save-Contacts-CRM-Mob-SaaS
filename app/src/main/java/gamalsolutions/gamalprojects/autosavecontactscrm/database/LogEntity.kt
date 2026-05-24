package gamalsolutions.gamalprojects.autosavecontactscrm.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "operation_logs")
data class LogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val phoneNumber: String,
    val name: String,
    val source: String,
    val status: String,
    val timestamp: Long,
    val details: String
)

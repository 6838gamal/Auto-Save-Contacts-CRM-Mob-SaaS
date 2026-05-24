package gamalsolutions.gamalprojects.autosavecontactscrm.database

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "contacts",
    indices = [Index(value = ["phoneNumber"], unique = true)]
)
data class ContactEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val phoneNumber: String,
    val source: String,
    val savedTimestamp: Long,
    val lastInteractionTimestamp: Long,
    val interactionCount: Int = 1
)

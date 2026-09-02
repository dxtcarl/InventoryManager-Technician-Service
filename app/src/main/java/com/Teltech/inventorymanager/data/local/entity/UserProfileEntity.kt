package com.Teltech.inventorymanager.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_profile")
data class UserProfileEntity(
    @PrimaryKey val id: Int = 0, // Single user profile, so fixed ID
    val name: String,
    val profilePictureUri: String?
)
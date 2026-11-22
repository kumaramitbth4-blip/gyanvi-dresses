package com.example.data.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

object AccountType {
    const val CUSTOMER = "CUSTOMER"
    const val SELLER = "SELLER"
}

/**
 * Room Entity storing registered Customer and Seller accounts locally.
 * Supports registration and login via Mobile Number OR Email ID (or both) with a password.
 */
@Entity(tableName = "user_accounts")
data class UserAccountEntity(
    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: String,

    @ColumnInfo(name = "account_type")
    val accountType: String, // AccountType.CUSTOMER or AccountType.SELLER

    @ColumnInfo(name = "full_name")
    val fullName: String,

    @ColumnInfo(name = "mobile_number")
    val mobileNumber: String = "",

    @ColumnInfo(name = "email")
    val email: String = "",

    @ColumnInfo(name = "password_hash")
    val passwordHash: String,

    @ColumnInfo(name = "shop_name")
    val shopName: String = "",

    @ColumnInfo(name = "business_address")
    val businessAddress: String = "",

    @ColumnInfo(name = "gstin")
    val gstin: String = "",

    @ColumnInfo(name = "is_verified")
    val isVerified: Boolean = true,

    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis()
)

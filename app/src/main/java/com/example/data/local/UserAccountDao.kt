package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface UserAccountDao {

    @Query("SELECT * FROM user_accounts ORDER BY created_at DESC")
    fun getAllAccounts(): Flow<List<UserAccountEntity>>

    @Query("SELECT * FROM user_accounts WHERE account_type = :accountType ORDER BY created_at DESC")
    fun getAccountsByType(accountType: String): Flow<List<UserAccountEntity>>

    @Query("SELECT * FROM user_accounts WHERE id = :id LIMIT 1")
    suspend fun getAccountById(id: String): UserAccountEntity?

    @Query(
        "SELECT * FROM user_accounts WHERE account_type = :accountType " +
            "AND ((:mobile != '' AND mobile_number = :mobile) OR (:email != '' AND LOWER(email) = LOWER(:email))) " +
            "LIMIT 1"
    )
    suspend fun findByContactAndType(
        accountType: String,
        mobile: String,
        email: String
    ): UserAccountEntity?

    @Query(
        "SELECT * FROM user_accounts WHERE account_type = :accountType " +
            "AND ((mobile_number != '' AND mobile_number = :identifier) OR (email != '' AND LOWER(email) = LOWER(:identifier))) " +
            "LIMIT 1"
    )
    suspend fun findByIdentifierAndType(
        accountType: String,
        identifier: String
    ): UserAccountEntity?

    @Query(
        "SELECT * FROM user_accounts WHERE " +
            "(mobile_number != '' AND mobile_number = :identifier) OR (email != '' AND LOWER(email) = LOWER(:identifier)) " +
            "ORDER BY created_at DESC LIMIT 1"
    )
    suspend fun findAnyByIdentifier(identifier: String): UserAccountEntity?

    @Query("SELECT COUNT(*) FROM user_accounts")
    suspend fun getAccountCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAccount(account: UserAccountEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAccounts(accounts: List<UserAccountEntity>)

    @Query("UPDATE user_accounts SET password_hash = :newPasswordHash WHERE id = :accountId")
    suspend fun updatePassword(accountId: String, newPasswordHash: String)

    @Query("DELETE FROM user_accounts WHERE id = :accountId")
    suspend fun deleteAccountById(accountId: String)
}

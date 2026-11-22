package com.example.data.local

import kotlinx.coroutines.flow.Flow
import java.security.MessageDigest
import java.util.UUID

sealed class AuthResult {
    data class Success(val account: UserAccountEntity, val message: String) : AuthResult()
    data class Error(val reason: String) : AuthResult()
}

object PasswordHasher {
    fun hash(password: String): String {
        val bytes = MessageDigest.getInstance("SHA-256")
            .digest("gyanvi_dresses_auth_v1_$password".toByteArray(Charsets.UTF_8))
        return bytes.joinToString("") { "%02x".format(it) }
    }
}

/**
 * Repository handling Customer and Seller registration, authentication via Mobile Number OR Email ID + Password,
 * and password reset stored persistently in Room.
 */
class UserAccountRepository(private val userAccountDao: UserAccountDao) {

    val allAccounts: Flow<List<UserAccountEntity>> = userAccountDao.getAllAccounts()

    fun getAccountsByType(type: String): Flow<List<UserAccountEntity>> =
        userAccountDao.getAccountsByType(type)

    suspend fun seedInitialAccountsIfEmpty() {
        if (userAccountDao.getAccountCount() == 0) {
            val now = System.currentTimeMillis()
            userAccountDao.insertAccounts(
                listOf(
                    UserAccountEntity(
                        id = "cust_default_01",
                        accountType = AccountType.CUSTOMER,
                        fullName = "Amit Kumar Patwa",
                        mobileNumber = "9128754982",
                        email = "kumaramitbth4@gmail.com",
                        passwordHash = PasswordHasher.hash("Gyanvi@123"),
                        businessAddress = "Ward No. 12, Bettiah, Bihar - 845438",
                        isVerified = true,
                        createdAt = now
                    ),
                    UserAccountEntity(
                        id = "seller_default_01",
                        accountType = AccountType.SELLER,
                        fullName = "Amit Kumar Patwa",
                        mobileNumber = "9835107144",
                        email = "seller@gyanvidresses.in",
                        passwordHash = PasswordHasher.hash("Seller@123"),
                        shopName = "Gyanvi Dresses Boutique",
                        businessAddress = "Lal Bazar, Main Road, Bettiah - 845438",
                        gstin = "10ABCDE1234F1Z5",
                        isVerified = true,
                        createdAt = now - 1000L
                    )
                )
            )
        }
    }

    suspend fun registerAccount(
        accountType: String,
        fullName: String,
        mobileNumber: String,
        email: String,
        password: String,
        shopName: String = "",
        businessAddress: String = "",
        gstin: String = ""
    ): AuthResult {
        val cleanName = fullName.trim()
        val cleanMobile = normalizeMobile(mobileNumber)
        val cleanEmail = email.trim().lowercase()

        if (cleanName.length < 2) {
            return AuthResult.Error("Please enter your full name (minimum 2 characters).")
        }
        if (cleanMobile.isBlank() && cleanEmail.isBlank()) {
            return AuthResult.Error("Please provide a Mobile Number or an Email ID.")
        }
        if (cleanMobile.isNotBlank() && !isValidMobile(cleanMobile)) {
            return AuthResult.Error("Please enter a valid 10-digit Mobile Number.")
        }
        if (cleanEmail.isNotBlank() && !isValidEmail(cleanEmail)) {
            return AuthResult.Error("Please enter a valid Email ID (e.g. name@example.com).")
        }
        if (password.length < 6) {
            return AuthResult.Error("Password must be at least 6 characters long.")
        }
        if (accountType == AccountType.SELLER && shopName.trim().length < 2) {
            return AuthResult.Error("Please enter your Shop / Boutique Name for Seller registration.")
        }

        val existing = userAccountDao.findByContactAndType(
            accountType = accountType,
            mobile = cleanMobile,
            email = cleanEmail
        )
        if (existing != null) {
            val roleLabel = if (accountType == AccountType.SELLER) "Seller" else "Customer"
            return AuthResult.Error("A $roleLabel account with this Mobile Number or Email ID already exists. Please Sign In.")
        }

        val newAccount = UserAccountEntity(
            id = "${accountType.lowercase()}_${UUID.randomUUID().toString().take(8)}",
            accountType = accountType,
            fullName = cleanName,
            mobileNumber = cleanMobile,
            email = cleanEmail,
            passwordHash = PasswordHasher.hash(password),
            shopName = if (accountType == AccountType.SELLER) shopName.trim() else "",
            businessAddress = businessAddress.trim(),
            gstin = gstin.trim().uppercase(),
            isVerified = true,
            createdAt = System.currentTimeMillis()
        )
        userAccountDao.insertAccount(newAccount)

        val roleTitle = if (accountType == AccountType.SELLER) "Seller Account" else "Customer Account"
        return AuthResult.Success(
            account = newAccount,
            message = "$roleTitle created successfully for $cleanName!"
        )
    }

    suspend fun login(
        identifier: String,
        password: String,
        accountType: String? = null
    ): AuthResult {
        val cleanIdentifier = normalizeIdentifier(identifier)
        if (cleanIdentifier.isBlank()) {
            return AuthResult.Error("Please enter your Mobile Number or Email ID.")
        }
        if (password.isBlank()) {
            return AuthResult.Error("Please enter your Password.")
        }

        val account = if (accountType != null) {
            userAccountDao.findByIdentifierAndType(accountType, cleanIdentifier)
                ?: userAccountDao.findAnyByIdentifier(cleanIdentifier)
        } else {
            userAccountDao.findAnyByIdentifier(cleanIdentifier)
        }

        if (account == null) {
            return AuthResult.Error("No account found for '$identifier'. Please register first.")
        }

        if (account.passwordHash != PasswordHasher.hash(password)) {
            return AuthResult.Error("Incorrect password. Try again or use Forgot Password via OTP.")
        }

        val roleLabel = if (account.accountType == AccountType.SELLER) "Seller" else "Customer"
        return AuthResult.Success(
            account = account,
            message = "Welcome back, ${account.fullName} ($roleLabel)!"
        )
    }

    suspend fun loginOrRegisterWithMobileOtp(
        mobileNumber: String,
        accountType: String = AccountType.CUSTOMER
    ): AuthResult {
        val cleanMobile = normalizeMobile(mobileNumber)
        if (!isValidMobile(cleanMobile)) {
            return AuthResult.Error("Please enter a valid 10-digit Mobile Number.")
        }

        val existing = userAccountDao.findByIdentifierAndType(accountType, cleanMobile)
            ?: userAccountDao.findAnyByIdentifier(cleanMobile)
        if (existing != null) {
            val roleLabel = if (existing.accountType == AccountType.SELLER) "Seller" else "Customer"
            return AuthResult.Success(
                account = existing,
                message = "OTP Verified! Logged in as ${existing.fullName} ($roleLabel)."
            )
        }

        if (accountType == AccountType.SELLER) {
            return AuthResult.Error("No Seller account found for +91 $cleanMobile. Please register Seller details first.")
        }

        val newCustomer = UserAccountEntity(
            id = "customer_${UUID.randomUUID().toString().take(8)}",
            accountType = AccountType.CUSTOMER,
            fullName = "Customer ${cleanMobile.takeLast(4)}",
            mobileNumber = cleanMobile,
            email = "",
            passwordHash = PasswordHasher.hash("OTP_${cleanMobile}"),
            shopName = "",
            businessAddress = "Bettiah, Bihar - 845438",
            gstin = "",
            isVerified = true,
            createdAt = System.currentTimeMillis()
        )
        userAccountDao.insertAccount(newCustomer)
        return AuthResult.Success(
            account = newCustomer,
            message = "OTP Verified! Logged in as Customer (+91 $cleanMobile)."
        )
    }

    suspend fun resetPassword(
        identifier: String,
        newPassword: String,
        accountType: String? = null
    ): AuthResult {
        val cleanIdentifier = normalizeIdentifier(identifier)
        if (cleanIdentifier.isBlank()) {
            return AuthResult.Error("Please enter your registered Mobile Number or Email ID.")
        }
        if (newPassword.length < 6) {
            return AuthResult.Error("New password must be at least 6 characters.")
        }

        val account = if (accountType != null) {
            userAccountDao.findByIdentifierAndType(accountType, cleanIdentifier)
                ?: userAccountDao.findAnyByIdentifier(cleanIdentifier)
        } else {
            userAccountDao.findAnyByIdentifier(cleanIdentifier)
        }

        if (account == null) {
            return AuthResult.Error("No registered account found matching '$identifier'.")
        }

        val newHash = PasswordHasher.hash(newPassword)
        userAccountDao.updatePassword(account.id, newHash)
        return AuthResult.Success(
            account = account.copy(passwordHash = newHash),
            message = "Password updated successfully! Logged in as ${account.fullName}."
        )
    }

    suspend fun getAccountById(id: String): UserAccountEntity? =
        userAccountDao.getAccountById(id)

    suspend fun deleteAccount(accountId: String) {
        userAccountDao.deleteAccountById(accountId)
    }

    companion object {
        fun normalizeMobile(raw: String): String {
            val digits = raw.filter { it.isDigit() }
            return when {
                digits.length == 12 && digits.startsWith("91") -> digits.drop(2)
                digits.length == 11 && digits.startsWith("0") -> digits.drop(1)
                else -> digits
            }
        }

        fun normalizeIdentifier(raw: String): String {
            val trimmed = raw.trim()
            return if (trimmed.contains("@")) {
                trimmed.lowercase()
            } else {
                val normalized = normalizeMobile(trimmed)
                if (normalized.isNotEmpty()) normalized else trimmed
            }
        }

        fun isValidMobile(digits: String): Boolean =
            digits.length == 10 && digits.all { it.isDigit() }

        fun isValidEmail(email: String): Boolean {
            val trimmed = email.trim()
            val atIdx = trimmed.indexOf('@')
            val dotIdx = trimmed.lastIndexOf('.')
            return atIdx > 0 && dotIdx > atIdx + 1 && dotIdx < trimmed.length - 1 && !trimmed.contains(" ")
        }
    }
}

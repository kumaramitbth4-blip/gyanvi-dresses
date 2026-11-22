package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.AccountType
import com.example.data.local.AuthResult
import com.example.data.local.GyanviDatabase
import com.example.data.local.UserAccountDao
import com.example.data.local.UserAccountRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class UserAuthDatabaseTest {

    private lateinit var database: GyanviDatabase
    private lateinit var userDao: UserAccountDao
    private lateinit var repository: UserAccountRepository

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, GyanviDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        userDao = database.userAccountDao()
        repository = UserAccountRepository(userDao)
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun customerRegistrationWithMobileOrEmailAndPasswordWorks() = runTest {
        // 1. Register Customer with Mobile Number only + Password
        val mobileReg = repository.registerAccount(
            accountType = AccountType.CUSTOMER,
            fullName = "Priya Sharma",
            mobileNumber = "+91 9876543210",
            email = "",
            password = "MySecretPassword1"
        )
        assertTrue(mobileReg is AuthResult.Success)
        val cust1 = (mobileReg as AuthResult.Success).account
        assertEquals(AccountType.CUSTOMER, cust1.accountType)
        assertEquals("9876543210", cust1.mobileNumber)
        assertNotEquals("MySecretPassword1", cust1.passwordHash)

        // 2. Register Customer with Email ID only + Password
        val emailReg = repository.registerAccount(
            accountType = AccountType.CUSTOMER,
            fullName = "Rohan Verma",
            mobileNumber = "",
            email = "Rohan.Verma@Example.com",
            password = "RohanPassword99"
        )
        assertTrue(emailReg is AuthResult.Success)
        val cust2 = (emailReg as AuthResult.Success).account
        assertEquals("rohan.verma@example.com", cust2.email)

        // 3. Login with Mobile + Password succeeds, wrong password fails
        val loginOk = repository.login("9876543210", "MySecretPassword1", AccountType.CUSTOMER)
        assertTrue(loginOk is AuthResult.Success)
        assertEquals("Priya Sharma", (loginOk as AuthResult.Success).account.fullName)

        val loginFail = repository.login("rohan.verma@example.com", "WrongPass", AccountType.CUSTOMER)
        assertTrue(loginFail is AuthResult.Error)
    }

    @Test
    fun sellerRegistrationLoginAndPasswordResetPersistedInRoom() = runTest {
        // 1. Register Seller with Shop Name, Mobile + Email + Password
        val sellerReg = repository.registerAccount(
            accountType = AccountType.SELLER,
            fullName = "Anjali Devi",
            mobileNumber = "9123456780",
            email = "anjali.boutique@gyanvi.in",
            password = "Boutique@123",
            shopName = "Maa Vaishno Saree Center",
            businessAddress = "Main Bazar, Bettiah - 845438",
            gstin = "10AAAAA0000A1Z5"
        )
        assertTrue(sellerReg is AuthResult.Success)
        val seller = (sellerReg as AuthResult.Success).account
        assertEquals(AccountType.SELLER, seller.accountType)
        assertEquals("Maa Vaishno Saree Center", seller.shopName)

        // 2. Duplicate seller registration with same mobile/email is rejected
        val duplicate = repository.registerAccount(
            accountType = AccountType.SELLER,
            fullName = "Duplicate Seller",
            mobileNumber = "9123456780",
            email = "",
            password = "Password123",
            shopName = "Another Shop"
        )
        assertTrue(duplicate is AuthResult.Error)

        // 3. Reset password & verify login with Email + new Password
        val resetRes = repository.resetPassword("anjali.boutique@gyanvi.in", "UpdatedPass@456", AccountType.SELLER)
        assertTrue(resetRes is AuthResult.Success)

        val oldPassLogin = repository.login("9123456780", "Boutique@123", AccountType.SELLER)
        assertTrue(oldPassLogin is AuthResult.Error)

        val newPassLogin = repository.login("anjali.boutique@gyanvi.in", "UpdatedPass@456", AccountType.SELLER)
        assertTrue(newPassLogin is AuthResult.Success)

        val sellersList = repository.getAccountsByType(AccountType.SELLER).first()
        assertEquals(1, sellersList.size)
    }
}

package com.example.util

import android.content.Context
import android.util.Log
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.NoCredentialException
import com.example.R
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.auth
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

private const val GOOGLE_AUTH_TAG = "GoogleSignInHelper"

fun FirebaseAuth.authStateFlow(): Flow<FirebaseUser?> = callbackFlow {
    val listener = FirebaseAuth.AuthStateListener { firebaseAuth ->
        trySend(firebaseAuth.currentUser)
    }
    addAuthStateListener(listener)
    awaitClose { removeAuthStateListener(listener) }
}

object GoogleSignInHelper {

    suspend fun attemptAutoSignIn(
        activityContext: Context,
        auth: FirebaseAuth = Firebase.auth,
        filterByAuthorizedAccounts: Boolean = true
    ): FirebaseUser? {
        auth.currentUser?.let { return it }
        return try {
            val webClientId = activityContext.getString(R.string.default_web_client_id)
            if (webClientId.isBlank()) return null

            val googleIdOption = GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(filterByAuthorizedAccounts)
                .setServerClientId(webClientId)
                .setAutoSelectEnabled(true)
                .build()

            val request = GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build()

            val credentialManager = CredentialManager.create(activityContext)
            val result = credentialManager.getCredential(
                request = request,
                context = activityContext
            )

            val cred = result.credential
            if (cred is CustomCredential &&
                cred.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
            ) {
                val googleCred = GoogleIdTokenCredential.createFrom(cred.data)
                val firebaseCred = GoogleAuthProvider.getCredential(googleCred.idToken, null)
                auth.signInWithCredential(firebaseCred).await().user
            } else {
                null
            }
        } catch (e: CancellationException) {
            throw e
        } catch (_: NoCredentialException) {
            null
        } catch (_: GetCredentialCancellationException) {
            null
        } catch (e: Exception) {
            Log.d(GOOGLE_AUTH_TAG, "Silent Google auto sign-in skipped: ${e.message}")
            null
        }
    }

    suspend fun signInWithGoogleExplicit(
        activityContext: Context,
        auth: FirebaseAuth = Firebase.auth
    ): Result<FirebaseUser> {
        return try {
            val webClientId = activityContext.getString(R.string.default_web_client_id)
            require(webClientId.isNotBlank()) { "Google Sign-In client ID missing in configuration." }

            val signInWithGoogleOption = GetSignInWithGoogleOption.Builder(webClientId).build()
            val request = GetCredentialRequest.Builder()
                .addCredentialOption(signInWithGoogleOption)
                .build()

            val credentialManager = CredentialManager.create(activityContext)
            val result = credentialManager.getCredential(
                request = request,
                context = activityContext
            )

            val credential = result.credential
            if (credential is CustomCredential &&
                credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
            ) {
                val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                val firebaseCredential = GoogleAuthProvider.getCredential(
                    googleIdTokenCredential.idToken,
                    null
                )
                val authResult = auth.signInWithCredential(firebaseCredential).await()
                val user = authResult.user
                    ?: return Result.failure(IllegalStateException("Google Sign-In returned empty user."))
                Result.success(user)
            } else {
                Result.failure(IllegalStateException("Unsupported credential type returned."))
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: GetCredentialCancellationException) {
            Result.failure(Exception("Google Sign-In cancelled by user."))
        } catch (e: Exception) {
            Log.e(GOOGLE_AUTH_TAG, "Google Sign-In failed", e)
            Result.failure(e)
        }
    }

    suspend fun signOutSafely(
        context: Context,
        auth: FirebaseAuth = Firebase.auth,
        beforeAuthSignOut: suspend () -> Unit = {}
    ) {
        withContext(NonCancellable) {
            runCatching { beforeAuthSignOut() }
            runCatching { auth.signOut() }
            runCatching {
                CredentialManager.create(context).clearCredentialState(ClearCredentialStateRequest())
            }
        }
    }
}

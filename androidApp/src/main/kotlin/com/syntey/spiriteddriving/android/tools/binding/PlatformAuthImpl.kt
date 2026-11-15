package com.syntey.spiriteddriving.android.tools.binding

import android.annotation.SuppressLint
import android.content.Context
import androidx.activity.ComponentActivity
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.Firebase
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.auth
import com.syntey.spiriteddriving.core.platform.binding.AuthListenerRegistration
import com.syntey.spiriteddriving.core.platform.binding.AuthUser
import com.syntey.spiriteddriving.core.platform.binding.AuthUserListener
import com.syntey.spiriteddriving.core.platform.binding.PlatformAuth
import com.syntey.spiriteddriving.core.platform.binding.SignInListener
import com.syntey.spiriteddriving.core.platform.binding.SignInProvider
import com.syntey.spiriteddriving.core.platform.binding.TokenListener
import kotlinx.coroutines.launch

class PlatformAuthImpl(private val context: Context) : PlatformAuth {

    private var activity: ComponentActivity? = null

    /** Lets the Google sign-in sheet show over [activity]. Call it from the activity's `onCreate`. */
    fun attach(activity: ComponentActivity) {
        this.activity = activity
        activity.lifecycle.addObserver(
            object : DefaultLifecycleObserver {
                override fun onDestroy(owner: LifecycleOwner) {
                    if (this@PlatformAuthImpl.activity === activity) {
                        this@PlatformAuthImpl.activity = null
                    }
                }
            },
        )
    }

    override fun currentUser(): AuthUser? = if (isConfigured()) Firebase.auth.currentUser?.toAuthUser() else null

    override fun addUserListener(listener: AuthUserListener): AuthListenerRegistration {
        if (!isConfigured()) {
            listener.onUser(null)
            return AuthListenerRegistration { }
        }
        val auth = Firebase.auth
        // Firebase calls the listener at once with the current user.
        val firebaseListener = FirebaseAuth.AuthStateListener { listener.onUser(it.currentUser?.toAuthUser()) }
        auth.addAuthStateListener(firebaseListener)
        return AuthListenerRegistration { auth.removeAuthStateListener(firebaseListener) }
    }

    override fun signIn(provider: SignInProvider, listener: SignInListener) {
        when (provider) {
            SignInProvider.Google -> signInWithGoogle(listener)
            SignInProvider.Apple -> listener.onFailed("Sign in with Apple isn't available on Android")
        }
    }

    override fun signOut() {
        if (isConfigured()) Firebase.auth.signOut()
        val activity = activity ?: return
        activity.lifecycleScope.launch {
            try {
                CredentialManager.create(activity).clearCredentialState(ClearCredentialStateRequest())
            } catch (_: Exception) {
                // Signing out of Firebase already happened, and the next Google sign-in shows the account picker anyway.
            }
        }
    }

    override fun idToken(listener: TokenListener) {
        val user = if (isConfigured()) Firebase.auth.currentUser else null
        if (user == null) {
            listener.onToken(null)
            return
        }
        user.getIdToken(false).addOnCompleteListener { listener.onToken(if (it.isSuccessful) it.result.token else null) }
    }

    private fun signInWithGoogle(listener: SignInListener) {
        val activity = activity
        val webClientId = webClientId()
        when {
            activity == null -> listener.onFailed(NO_ACTIVITY_MESSAGE)
            !isConfigured() || webClientId == null -> listener.onFailed(NOT_CONFIGURED_MESSAGE)
            else -> activity.lifecycleScope.launch { exchangeGoogleCredential(activity, webClientId, listener) }
        }
    }

    private suspend fun exchangeGoogleCredential(activity: ComponentActivity, webClientId: String, listener: SignInListener) {
        runCatching { requestGoogleIdToken(activity, webClientId) }
            .onFailure { error ->
                if (error is GetCredentialCancellationException) {
                    listener.onCancelled()
                } else {
                    listener.onFailed(error.message ?: SIGN_IN_FAILED_MESSAGE)
                }
            }
            .onSuccess { idToken ->
                Firebase.auth.signInWithCredential(GoogleAuthProvider.getCredential(idToken, null))
                    .addOnCompleteListener { task ->
                        val exception = task.exception
                        when {
                            task.isSuccessful -> listener.onSignedIn()

                            exception is FirebaseAuthUserCollisionException ->
                                listener.onAccountExistsWithOtherProvider(SignInProvider.Apple)

                            else -> listener.onFailed(exception?.message ?: SIGN_IN_FAILED_MESSAGE)
                        }
                    }
            }
    }

    private suspend fun requestGoogleIdToken(activity: ComponentActivity, webClientId: String): String {
        val request = GetCredentialRequest.Builder()
            .addCredentialOption(GetSignInWithGoogleOption.Builder(serverClientId = webClientId).build())
            .build()
        val credential = CredentialManager.create(activity).getCredential(activity, request).credential
        check(credential is CustomCredential && credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
            SIGN_IN_FAILED_MESSAGE
        }
        return GoogleIdTokenCredential.createFrom(credential.data).idToken
    }

    // The google-services plugin generates this string only when google-services.json is present, so R.string can't name it.
    @SuppressLint("DiscouragedApi")
    private fun webClientId(): String? {
        val id = context.resources.getIdentifier(WEB_CLIENT_ID_RESOURCE, "string", context.packageName)
        return if (id == 0) null else context.getString(id)
    }

    // Firebase isn't initialised without google-services.json, see the build's missingGoogleServicesStrategy.
    private fun isConfigured() = FirebaseApp.getApps(context).isNotEmpty()

    private fun FirebaseUser.toAuthUser() = AuthUser(
        id = uid,
        name = displayName?.takeIf { it.isNotBlank() },
        email = email?.takeIf { it.isNotBlank() },
        provider = if (providerData.any { it.providerId == APPLE_PROVIDER_ID }) SignInProvider.Apple else SignInProvider.Google,
    )

    private companion object {
        const val APPLE_PROVIDER_ID = "apple.com"
        const val WEB_CLIENT_ID_RESOURCE = "default_web_client_id"
        const val NOT_CONFIGURED_MESSAGE = "Firebase isn't configured"
        const val NO_ACTIVITY_MESSAGE = "Sign-in isn't available right now"
        const val SIGN_IN_FAILED_MESSAGE = "Sign-in failed"
    }
}

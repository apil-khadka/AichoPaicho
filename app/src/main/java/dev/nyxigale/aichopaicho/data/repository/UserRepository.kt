package dev.nyxigale.aichopaicho.data.repository

import android.net.Uri
import androidx.core.net.toUri
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import dev.nyxigale.aichopaicho.data.dao.UserDao
import dev.nyxigale.aichopaicho.data.entity.User
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepository @Inject constructor(
    private val userDao: UserDao,
    private val firestore: FirebaseFirestore
) {

    sealed interface SignInResolution {
        data class Allowed(
            val user: User,
            val recoveredFromSoftDelete: Boolean
        ) : SignInResolution

        data class RecoveryWindowExpired(
            val deletedAt: Long?
        ) : SignInResolution
    }

    suspend fun upsert(user: User) {
        userDao.upsert(user)
    }

    suspend fun softDelete(id: String, updatedAt: Long) {
        userDao.softDelete(id, updatedAt)
    }

    suspend fun getUser(): User {
        return try {
            userDao.getUser()
        } catch (e: Exception) {
            // When no user is in the DB, Room throws an exception for a non-null return type.
            // We catch it and return a default "sentinel" user to avoid a crash.
            User(id = "", name = null, email = null, photoUrl = null, isOffline = true)
        }
    }

    suspend fun deleteUserCompletely(userId: String) {
        userDao.deleteUserCompletely(userId)
    }

    suspend fun resolveUserForSignIn(firebaseUser: FirebaseUser): SignInResolution {
        val now = System.currentTimeMillis()
        val document = firestore.collection(USERS_COLLECTION)
            .document(firebaseUser.uid)
            .get()
            .await()

        val remoteDeleted = document.getBoolean("deleted")
            ?: document.getBoolean("isDeleted")
            ?: false
        val remoteCreatedAt = document.readMillis("createdAt") ?: now
        val remoteUpdatedAt = document.readMillis("updatedAt") ?: remoteCreatedAt

        if (remoteDeleted && !isWithinRecoveryWindow(remoteUpdatedAt, now)) {
            return SignInResolution.RecoveryWindowExpired(deletedAt = remoteUpdatedAt)
        }

        val resolvedName = firebaseUser.displayName ?: document.getString("name")
        val resolvedEmail = firebaseUser.email ?: document.getString("email")
        val resolvedPhotoUrl = firebaseUser.photoUrl ?: document.readPhotoUri("photoUrl")

        val resolvedUser = User(
            id = firebaseUser.uid,
            name = resolvedName,
            email = resolvedEmail,
            photoUrl = resolvedPhotoUrl,
            isDeleted = false,
            isOffline = false,
            createdAt = remoteCreatedAt,
            updatedAt = now
        )

        return SignInResolution.Allowed(
            user = resolvedUser,
            recoveredFromSoftDelete = remoteDeleted
        )
    }

    suspend fun upsertRemoteUser(user: User) {
        val payload = mapOf(
            "id" to user.id,
            "name" to user.name,
            "email" to user.email,
            "photoUrl" to user.photoUrl?.toString(),
            "deleted" to user.isDeleted,
            "offline" to user.isOffline,
            "createdAt" to user.createdAt,
            "updatedAt" to user.updatedAt
        )

        firestore.collection(USERS_COLLECTION)
            .document(user.id)
            .set(payload, SetOptions.merge())
            .await()
    }

    private fun isWithinRecoveryWindow(deletedAt: Long, now: Long): Boolean {
        return (now - deletedAt) <= RECOVERY_WINDOW_MILLIS
    }

    private fun DocumentSnapshot.readMillis(field: String): Long? {
        return getLong(field) ?: getTimestamp(field)?.toDate()?.time
    }

    private fun DocumentSnapshot.readPhotoUri(field: String): Uri? {
        val value = get(field) ?: return null
        return when (value) {
            is Uri -> value
            is String -> value.takeIf { it.isNotBlank() }?.toUri()
            else -> null
        }
    }

    companion object {
        private const val USERS_COLLECTION = "users"
        private const val RECOVERY_WINDOW_MILLIS = 30L * 24L * 60L * 60L * 1000L
    }
}

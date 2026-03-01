package dev.nyxigale.aichopaicho.data.mapper

import android.net.Uri
import com.google.firebase.auth.FirebaseUser
import dev.nyxigale.aichopaicho.data.entity.User
import io.mockk.every
import io.mockk.mockk
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class UserMapperTest {

    @Test
    fun `toUserEntity maps FirebaseUser to User correctly`() {
        val mockUri = mockk<Uri>()
        val mockFirebaseUser = mockk<FirebaseUser> {
            every { uid } returns "test_uid_123"
            every { email } returns "test@example.com"
            every { displayName } returns "Test User"
            every { photoUrl } returns mockUri
        }

        val beforeTime = System.currentTimeMillis()
        val userEntity = mockFirebaseUser.toUserEntity()
        val afterTime = System.currentTimeMillis()

        assertEquals("test_uid_123", userEntity.id)
        assertEquals("test@example.com", userEntity.email)
        assertEquals("Test User", userEntity.name)
        assertEquals(mockUri, userEntity.photoUrl)

        assertTrue("createdAt should be >= beforeTime", userEntity.createdAt >= beforeTime)
        assertTrue("createdAt should be <= afterTime", userEntity.createdAt <= afterTime)
        assertTrue("updatedAt should be >= beforeTime", userEntity.updatedAt >= beforeTime)
        assertTrue("updatedAt should be <= afterTime", userEntity.updatedAt <= afterTime)
    }

    @Test
    fun `toUserEntity handles null values correctly`() {
        val mockFirebaseUser = mockk<FirebaseUser> {
            every { uid } returns "test_uid_456"
            every { email } returns null
            every { displayName } returns null
            every { photoUrl } returns null
        }

        val userEntity = mockFirebaseUser.toUserEntity()

        assertEquals("test_uid_456", userEntity.id)
        assertEquals(null, userEntity.email)
        assertEquals(null, userEntity.name)
        assertEquals(null, userEntity.photoUrl)
    }
}

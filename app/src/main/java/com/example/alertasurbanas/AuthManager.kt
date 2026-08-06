package com.example.alertasurbanas

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

data class UserProfile(
    val uid: String = "",
    val name: String = "",
    val email: String = "",
    val role: String = "citizen"
) {
    val roleLabel: String
        get() = if (role == "admin") "Administrador" else "Ciudadano"

    val initials: String
        get() {
            val source = name.ifBlank { email }.trim()
            if (source.isBlank()) return "GN"

            val parts = source
                .replace("@.*".toRegex(), "")
                .split(" ", ".", "_", "-")
                .filter { it.isNotBlank() }

            return parts
                .take(2)
                .map { it.first().uppercaseChar() }
                .joinToString("")
                .ifBlank { "GN" }
        }
}

class AuthManager {
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    suspend fun login(
        email: String,
        password: String
    ): String {
        val result = auth.signInWithEmailAndPassword(email, password).await()
        val uid = result.user?.uid ?: throw Exception("No se encontró el usuario")

        val userDoc = db.collection("users")
            .document(uid)
            .get()
            .await()

        return userDoc.getString("role") ?: "citizen"
    }

    suspend fun getCurrentUserProfile(): UserProfile {
        val user = auth.currentUser ?: throw Exception("No hay usuario activo")
        val userDoc = db.collection("users")
            .document(user.uid)
            .get()
            .await()

        return UserProfile(
            uid = user.uid,
            name = userDoc.getString("name") ?: user.displayName.orEmpty(),
            email = userDoc.getString("email") ?: user.email.orEmpty(),
            role = userDoc.getString("role") ?: "citizen"
        )
    }

    suspend fun registerCitizen(
        name: String,
        email: String,
        password: String
    ) {
        val result = auth.createUserWithEmailAndPassword(email, password).await()
        val uid = result.user?.uid ?: throw Exception("No se pudo crear el usuario")

        val userData = hashMapOf(
            "name" to name,
            "email" to email,
            "role" to "citizen"
        )

        db.collection("users")
            .document(uid)
            .set(userData)
            .await()

        result.user?.updateProfile(
            UserProfileChangeRequest.Builder()
                .setDisplayName(name)
                .build()
        )?.await()
    }

    suspend fun updateCurrentProfile(
        name: String,
        email: String,
        currentPassword: String
    ): UserProfile {
        val user = auth.currentUser ?: throw Exception("No hay usuario activo")
        val cleanName = name.trim()
        val cleanEmail = email.trim()
        val currentEmail = user.email.orEmpty()

        if (cleanName.isBlank()) {
            throw Exception("El nombre no puede estar vacío")
        }

        if (cleanEmail.isBlank()) {
            throw Exception("El correo no puede estar vacío")
        }

        if (!cleanEmail.equals(currentEmail, ignoreCase = true)) {
            if (currentPassword.isBlank()) {
                throw Exception("Para cambiar el correo debes escribir tu contraseña actual")
            }

            val credential = EmailAuthProvider.getCredential(currentEmail, currentPassword)
            user.reauthenticate(credential).await()
            user.updateEmail(cleanEmail).await()
        }

        user.updateProfile(
            UserProfileChangeRequest.Builder()
                .setDisplayName(cleanName)
                .build()
        ).await()

        db.collection("users")
            .document(user.uid)
            .update(
                mapOf(
                    "name" to cleanName,
                    "email" to cleanEmail
                )
            )
            .await()

        return getCurrentUserProfile()
    }

    suspend fun changeCurrentPassword(
        currentPassword: String,
        newPassword: String
    ) {
        val user = auth.currentUser ?: throw Exception("No hay usuario activo")
        val currentEmail = user.email ?: throw Exception("No se encontró el correo del usuario")

        if (currentPassword.isBlank()) {
            throw Exception("Escribe tu contraseña actual")
        }

        if (newPassword.length < 6) {
            throw Exception("La nueva contraseña debe tener al menos 6 caracteres")
        }

        val credential = EmailAuthProvider.getCredential(currentEmail, currentPassword)
        user.reauthenticate(credential).await()
        user.updatePassword(newPassword).await()
    }

    suspend fun getAllUserProfiles(): List<UserProfile> {
        return db.collection("users")
            .get()
            .await()
            .documents
            .map { document ->
                UserProfile(
                    uid = document.id,
                    name = document.getString("name").orEmpty(),
                    email = document.getString("email").orEmpty(),
                    role = document.getString("role") ?: "citizen"
                )
            }
            .sortedWith(compareBy<UserProfile> { it.role != "admin" }.thenBy { it.name.ifBlank { it.email } })
    }

    suspend fun updateUserRole(
        userId: String,
        role: String
    ) {
        if (userId.isBlank()) {
            throw Exception("No se encontro el usuario")
        }

        if (role !in listOf("admin", "citizen")) {
            throw Exception("Rol no valido")
        }

        db.collection("users")
            .document(userId)
            .update("role", role)
            .await()
    }

    fun logout() {
        auth.signOut()
    }

    fun isLoggedIn(): Boolean {
        return auth.currentUser != null
    }
}

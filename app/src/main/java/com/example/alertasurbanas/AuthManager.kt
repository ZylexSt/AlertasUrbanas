package com.example.alertasurbanas

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

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
    }

    fun logout() {
        auth.signOut()
    }

    fun isLoggedIn(): Boolean {
        return auth.currentUser != null
    }
}
package com.example.pacmanclone.util

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions

fun updateUserHighScores(
    newScore: Int,
    onSuccess: () -> Unit = {},
    onFailure: (Exception) -> Unit = {}
) {
    val user = FirebaseAuth.getInstance().currentUser ?: return
    val db = FirebaseFirestore.getInstance()
    val userDoc = db.collection("users").document(user.uid)

    userDoc.get().addOnSuccessListener { snapshot ->
        // Get the current top scores as a list of Long
        val currentScores = snapshot.get("topScores") as? List<Long> ?: emptyList()
        // Convert to mutable list of Int
        val updatedScores = currentScores.map { it.toInt() }.toMutableList()
        updatedScores.add(newScore)
        // Sort descending and take top 10
        val top10 = updatedScores.sortedDescending().take(10)
        // Best score is the highest
        val bestScore = top10.firstOrNull() ?: newScore
        // IMPORTANT: Use 'set(..., SetOptions.merge())' or 'update(...)'
        userDoc.set(
            mapOf(
                "topScores" to top10.map { it.toLong() },
                "bestScore" to bestScore.toLong()
            ),
            SetOptions.merge() // <--- Merges instead of overwriting
        )
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onFailure(it) }
    }.addOnFailureListener { onFailure(it) }
}

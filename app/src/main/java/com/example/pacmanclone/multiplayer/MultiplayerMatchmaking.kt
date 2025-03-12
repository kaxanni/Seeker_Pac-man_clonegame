package com.example.pacmanclone.multiplayer

import com.example.pacmanclone.game.createOriginalMaze
import com.example.pacmanclone.game.PELLET
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.delay
import kotlinx.coroutines.tasks.await

data class MultiplayerMatch(
    val matchId: String = "",
    val pacmanUid: String = "",
    val ghostUid: String = "",
    val status: String = "active"
)

/**
 * Updated joinMatchmaking(selectedRole):
 *  - If user picks "pacman", only match them with a "ghost" in the lobby, and vice versa.
 *  - If none found, add user to the lobby with chosen role, poll for up to 10s, then return null if still unmatched.
 */
suspend fun joinMatchmaking(selectedRole: String): MultiplayerMatch? {
    val auth = FirebaseAuth.getInstance()
    val currentUser = auth.currentUser ?: return null
    val db = FirebaseFirestore.getInstance()

    // Opposite role: "pacman" <-> "ghost"
    val oppositeRole = if (selectedRole == "pacman") "ghost" else "pacman"

    // 1) Look in the lobby for someone who has the opposite role
    val lobbyQuery = db.collection("lobby")
        .whereNotEqualTo("uid", currentUser.uid)
        .whereEqualTo("selectedRole", oppositeRole)
        .orderBy("uid")
        .orderBy("timestamp")
        .limit(1)
        .get()
        .await()

    if (!lobbyQuery.isEmpty) {
        // Found a waiting user with the opposite role
        val waitingDoc = lobbyQuery.documents.first()
        val waitingUid = waitingDoc.getString("uid") ?: return null
        // Remove them from the lobby
        waitingDoc.reference.delete().await()

        // If I chose pacman => I'm pacmanUid, they are ghostUid
        val (pacmanUid, ghostUid) = if (selectedRole == "pacman") {
            currentUser.uid to waitingUid
        } else {
            waitingUid to currentUser.uid
        }

        // Create match document with initial state
        val matchRef = db.collection("multiplayerMatches").document()
        val allPellets = mutableListOf<String>()
        val initialMaze = createOriginalMaze()
        for (r in initialMaze.indices) {
            for (c in initialMaze[r].indices) {
                if (initialMaze[r][c] == PELLET) {
                    allPellets.add("$r,$c")
                }
            }
        }
        // Pac-Man starts at (17,13), Ghost starts at (5,13)
        val matchData = mapOf(
            "matchId" to matchRef.id,
            "pacmanUid" to pacmanUid,
            "ghostUid" to ghostUid,
            "status" to "active",
            "pacmanPos" to listOf(17, 13),
            "ghostPos" to listOf(5, 13),
            "pacmanLives" to 2,
            "pelletCount" to 0,
            "pellets" to allPellets,
            "gameOver" to false,
            "ghostInvertedControl" to false
        )
        matchRef.set(matchData, SetOptions.merge()).await()
        return MultiplayerMatch(matchRef.id, pacmanUid, ghostUid, "active")
    } else {
        // 2) No opposite user found => add myself to the lobby, then poll for up to 10s
        return createLobbyAndPoll(db, currentUser.uid, selectedRole)
    }
}

private suspend fun createLobbyAndPoll(
    db: FirebaseFirestore,
    uid: String,
    selectedRole: String
): MultiplayerMatch? {
    // Put myself in the lobby
    val lobbyRef = db.collection("lobby").document(uid)
    val data = mapOf(
        "uid" to uid,
        "selectedRole" to selectedRole,
        "timestamp" to System.currentTimeMillis()
    )
    lobbyRef.set(data).await()

    // Poll for up to 10s (5 iterations, 2s each)
    repeat(5) {
        delay(2000L)
        // Check if I'm now in a match doc
        val matchQueryPac = db.collection("multiplayerMatches")
            .whereEqualTo("pacmanUid", uid)
            .limit(1)
            .get()
            .await()
        if (!matchQueryPac.isEmpty) {
            lobbyRef.delete().await()
            return matchQueryPac.documents.first().toObject(MultiplayerMatch::class.java)
        }
        val matchQueryGhost = db.collection("multiplayerMatches")
            .whereEqualTo("ghostUid", uid)
            .limit(1)
            .get()
            .await()
        if (!matchQueryGhost.isEmpty) {
            lobbyRef.delete().await()
            return matchQueryGhost.documents.first().toObject(MultiplayerMatch::class.java)
        }
    }
    // If no match after 10s, remove myself from lobby, return null
    lobbyRef.delete().await()
    return null
}

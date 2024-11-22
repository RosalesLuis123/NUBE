package com.example.nube

import android.content.Context

object UserManager {
    private const val PREFS_NAME = "user_prefs"
    private const val KEY_USERNAME = "username"
    private const val KEY_PLAN = "plan"

    fun saveUser(context: Context, user: User) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        with(prefs.edit()) {
            putString(KEY_USERNAME, user.username)
            putString(KEY_PLAN, user.plan)
            apply()
        }
    }

    fun getUser(context: Context): User? {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val username = prefs.getString(KEY_USERNAME, null) ?: return null
        val plan = prefs.getString(KEY_PLAN, "FREE") ?: "FREE"
        return User(username, "", plan)
    }
}

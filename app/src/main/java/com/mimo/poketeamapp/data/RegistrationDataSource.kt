package com.mimo.poketeamapp.data

import android.util.Log
import com.mimo.poketeamapp.database.AppDatabase
import com.mimo.poketeamapp.database.entity.User
import java.io.IOException

class RegistrationDataSource(private val db: AppDatabase) {

    fun registrate(name: String, surname: String, email: String, password: String): Result<*> {
        return try {
            val userDao = db.userDao()

            if(db.userDao().doesUserExist(email) == 0) {
                userDao.insertUser(name, surname, email, password)
                Result.Success("Ok")
            } else {
                Result.Error(IOException("Error in registration"))
            }
        } catch (e: Throwable) {
            Result.Error(IOException("Error in registration", e))
        }
    }

}
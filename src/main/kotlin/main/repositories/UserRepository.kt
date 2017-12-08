package main.repositories

import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import java.sql.Timestamp
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.ZonedDateTime
import javax.xml.bind.annotation.adapters.HexBinaryAdapter


data class User(var id: Int?, var email: String?, var password: String?,
                var salt: String?, var roles: List<Int>?,
                var reset: Timestamp?) {
    constructor() : this(null, null, null, null, null, null)
}

fun stringHash(message: String): String {
    val digest = MessageDigest.getInstance("SHA-256")
    val hashVal = (HexBinaryAdapter()).marshal(
            digest.digest(message.toByteArray(StandardCharsets.UTF_8)))
    return hashVal
}

object UserRepository {
    init {

    }

    fun signup(email: String, password: String?, role: Int) {
        var u = User(null, email, password, null, listOf<Int>(1), null)
        val conn = DB.connection()
        var statement = conn.prepareStatement("SELECT * FROM USERS where email = ?")
        statement.setString(1, email)
        val resultSet = statement.executeQuery()
        var result = DB.getResults(resultSet, User::class)
        if (result.size > 0) {
            throw Exception("User Already Exists")
        }

        var saltHash = stringHash(LocalDateTime.now().toString())
        var saltedPassword = password + saltHash
        val passwordHash = stringHash(saltedPassword)
        u.salt = saltHash
        u.password = passwordHash


        var array = conn.createArrayOf("INTEGER", u.roles!!.toTypedArray())
        statement = conn.prepareStatement("INSERT INTO USERS (email, password, salt, roles) VALUES (?, ?, ?, ?)")
        statement.setString(1, u.email)
        statement.setString(2, u.password)
        statement.setString(3, u.salt)
        statement.setArray(4, array)
        statement.executeUpdate()
        conn.close()
    }

    fun valid(email: String, password: String): User? {
        val result = getUser(email)
        if (result.size == 0) {
            return null
        }
        if (stringHash(password + result[0].salt) != result[0].password) {
            return null
        }
        return result[0]
    }

    private fun getUser(email: String): List<User> {
        val conn = DB.connection()
        var statement = conn.prepareStatement("SELECT * FROM USERS where email = ?")
        statement.setString(1, email)
        val resultSet = statement.executeQuery()
        var result = DB.getResults(resultSet, User::class)
        return result
    }

    fun changePassword(email: String, oldPassword: String, newPassword: String) {
        var user = valid(email, oldPassword)
        var newHashedPassword = stringHash(user!!.salt + newPassword)
        val conn = DB.connection()
        val statement = conn.prepareStatement("UPDATE USERS SET salt = ? where email = ?")
        statement.setString(1, newHashedPassword)
        statement.setString(2, email)
        statement.executeUpdate()
        conn.close()

    }

    fun addRole(email: String, password: String, role: Int) {
        var user = valid(email, password)
        var hasRole = false
        for (r in user!!.roles!!) {
            if (role == r) {
                hasRole = true
            }
        }
        if (!hasRole) {
            var mutableList = user.roles!!.toMutableList()
            mutableList.add(role)
            updateRole(mutableList, email)
        }

    }

    private fun updateRole(mutableList: MutableList<Int>, email: String) {
        val conn = DB.connection()
        var array = conn.createArrayOf("INTEGER", mutableList.toTypedArray())
        val statement = conn.prepareStatement("UPDATE USERS SET role = ? WHERE email = ?")
        statement.setArray(1, array)
        statement.setString(2, email)
        statement.executeUpdate()
        conn.close()
    }

    fun removeRole(email: String, password: String, role: Int) {
        var user = valid(email, password)
        var mutableList = user!!.roles!!.toMutableList()
        var elementCounter = 0
        for (r in mutableList) {
            if (r == role) {
                mutableList.removeAt(elementCounter)
                updateRole(mutableList, email)
            }
            elementCounter++
        }
    }

    fun requestReset(email: String): String {
        val conn = DB.connection()
        val curDate = Timestamp.from(ZonedDateTime.now(ZoneOffset.UTC).toInstant())
        val statement = conn.prepareStatement("UPDATE users SET reset = ? WHERE email = ?")
        statement.setTimestamp(1, curDate)
        statement.setString(2, email)
        statement.executeUpdate()
        conn.close()
        return stringHash(email + curDate.toString())
    }

    fun isValidationString(email: String, validationString: String): Boolean {
        val conn = DB.connection()
        val result = this.getUser(email)
        var user = result[0]
        var email = user.email
        var reset = user.reset
        var date = reset?.toLocalDateTime()?.toLocalDate();
        if (LocalDateTime.now().minusHours(1).toLocalDate() > date) {
            return false;
        }
        return stringHash(email + reset) == validationString
    }

    fun resetPassword(email: String, password: String, validationString: String) {
        if (isValidationString(email, validationString)) {
            val result = getUser(email)[0]
            val conn = DB.connection()
            val statement = conn.prepareStatement("UPDATE users SET password = ? WHERE email = ?")
            statement.setString(1, stringHash(result.salt + password))
            statement.setString(2, email)
            statement.executeUpdate()
            conn.close()
        } else {
            throw Exception("Invalid Reset Token")
        }
    }

}
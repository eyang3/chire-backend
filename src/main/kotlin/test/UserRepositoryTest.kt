package test

import main.repositories.DB
import main.repositories.UserRepository
import org.junit.Assert.*

class UserRepositoryTest {
    val db = DB
    @org.junit.Before
    fun setUp() {
        db.connect("jdbc:postgresql://127.0.0.1/chire", "postgres", "tcinTE5%k")
    }

    @org.junit.After
    fun tearDown() {
    }

    @org.junit.Test
    fun signup() {
        var m = UserRepository
        m.requestReset("iamspazzy@gmail.com")
        assert(1 == 1)
    }


    @org.junit.Test
    fun valid() {
        assert(1 == 1)
    }

    @org.junit.Test
    fun changePassword() {
    }

    @org.junit.Test
    fun addRole() {
    }

    @org.junit.Test
    fun removeRole() {
    }

    @org.junit.Test
    fun requestReset() {
    }

    @org.junit.Test
    fun isValidationString() {
    }

    @org.junit.Test
    fun resetPassword() {
    }

}
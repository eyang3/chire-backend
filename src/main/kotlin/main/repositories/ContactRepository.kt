package main.repositories

import repositories.Repository
import java.sql.Date
import java.sql.ResultSet

data class Contact(var id: Int?, var userref: Int?, var email: String?, var name: String?, var label: String?) {
    constructor() : this(null, null, null, null, null)
}

object ContactRepository: Repository()  {
    val tableName: String = "contacts"
    init {

    }
    fun create(userRef: Int, email: String, name: String?, label: String?): Int {
        var contact = Contact(userref = userRef, email = email, name = name, label = label, id = null)
        return (DB.crudSave(this.tableName, Contact::class, contact, null));
    }

    fun update(id: Int, userRef: Int, email: String, name: String?, label: String?) {
        var contact = Contact(userref = userRef, email = email, name = name, label = label, id = id)
        DB.crudSave(this.tableName, Contact::class, contact, id)
    }




}
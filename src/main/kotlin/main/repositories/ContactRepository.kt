package main.repositories

import repositories.Repository

data class Contact(var id: Int?, var userref: Int?, var email: String?, var name: String?, var label: String?) {
    constructor() : this(null, null, null, null, null)
}

object ContactRepository : Repository() {
    override val table: String = "contacts"

    init {

    }

    fun create(userRef: Int, email: String, name: String?, label: String?): Int {
        val contact = Contact(userref = userRef, email = email, name = name, label = label, id = null)
        return (DB.crudSave(this.table, Contact::class, contact, null));
    }

    fun update(id: Int, userRef: Int, email: String, name: String?, label: String?) {
        var contact = Contact(userref = userRef, email = email, name = name, label = label, id = id)
        DB.crudSave(this.table, Contact::class, contact, id)
    }


}
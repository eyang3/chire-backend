package main.repositories

import repositories.Repository

data class Contact(var id: Int?, var userref: Int?, var contactref: Int?, var email: String?, var name: String?, var label: String?) {
    constructor() : this(null, null, null, null, null, null)
}

object ContactRepository : Repository() {
    override val table: String = "contacts"

    init {

    }

    fun create(userRef: Int, email: String, name: String?, label: String?, contactref: Int?): Int {
        val contact = Contact(userref = userRef, contactref = contactref, email = email, name = name, label = label, id = null)
        return (DB.crudSave(this.table, Contact::class, contact, null));
    }

    fun update(id: Int, userRef: Int, email: String, name: String?, label: String?, contactRef: Int) {
        var contact = Contact(userref = userRef, email = email, name = name, label = label, id = id, contactref = contactRef)
        DB.crudSave(this.table, Contact::class, contact, id)
    }


}
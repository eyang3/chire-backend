package main.repositories

import java.sql.ResultSet

data class Jobs(var id: Int?, var title: String?, var salary: String?,
                var userref: Int?, var body: String?) {
    constructor() : this(null, null, null, null, null)
}

object JobRepository {
    init {

    }
    fun create(title: String, salary: String, userRef: Int, body: String): Int {
        var job = Jobs(null, title, salary, userRef, body);
        return(DB.crudSave("jobs", Jobs::class, job, null));
    }
    fun read(pattern: Jobs): ResultSet {
        return DB.crudRead("jobs", Jobs::class, pattern)

    }
    fun update(id: Int, title: String?, salary: String?, userRef: Int?, body: String?) {
        var job = Jobs(id, title, salary, userRef, body);
        DB.crudSave("jobs", Jobs::class, job, id)
    }
    fun delete(id: Int) {
        DB.crudDelete("jobs", id)
    }

}
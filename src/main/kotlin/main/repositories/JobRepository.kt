package main.repositories

import java.sql.Date
import java.sql.ResultSet

data class Jobs(var id: Int?, var title: String?, var salary: String?,
                var userref: Int?, var body: String?, var keywords: String?,
                var category: String?, var last_modified: Date?) {
    constructor() : this(null, null, null, null, null, null, null, null)
}

object JobRepository {
    init {

    }
    fun create(title: String, salary: String, userRef: Int, body: String, keywords: String?, category: String?): Int {
        var job = Jobs(null, title, salary, userRef, body, keywords, category, null)
        return (DB.crudSave("jobs", Jobs::class, job, null));
    }

    fun read(pattern: Jobs, subset: String = "", limit: String = "100", offset: String = "0"): ResultSet {
        return DB.crudRead("jobs", Jobs::class, pattern, subset = subset, limit = limit, offset = offset)
    }

    fun update(id: Int, title: String?, salary: String?, userRef: Int?, body: String?,
               keywords: String?, category: String?) {
        var job = Jobs(id, title, salary, userRef, body, keywords, category, null)
        DB.crudSave("jobs", Jobs::class, job, id)
    }

    fun delete(id: Int) {
        DB.crudDelete("jobs", id)
    }

}
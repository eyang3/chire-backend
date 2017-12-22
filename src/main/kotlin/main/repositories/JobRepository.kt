package main.repositories

import java.sql.ResultSet

data class Jobs(var id: Int?, var title: String?, var salary: String?,
                var userref: Int?, var body: String?, var keywords: String?, var category: String?) {
    constructor() : this(null, null, null, null, null, null, null)
}

object JobRepository {
    init {

    }
    fun create(title: String, salary: String, userRef: Int, body: String, keywords: String, category: String): Int {
        var job = Jobs(null, title, salary, userRef, body, keywords, category)
        return(DB.crudSave("jobs", Jobs::class, job, null));
    }
    fun read(pattern: Jobs, subset: String = ""): ResultSet {
        return DB.crudRead("jobs", Jobs::class, pattern, subset = subset)
    }
    fun update(id: Int, title: String?, salary: String?, userRef: Int?, body: String?,
               keywords: String?, category: String?) {
        var job = Jobs(id, title, salary, userRef, body, keywords,  category)
        DB.crudSave("jobs", Jobs::class, job, id)
    }
    fun delete(id: Int) {
        DB.crudDelete("jobs", id)
    }

}
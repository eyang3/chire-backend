package main.repositories;

import java.sql.ResultSet

data class Comments(var id: Int?, var evaluatorref: Int?, var applicationref: Int?, var comment: String?) {
    constructor() : this(null, null, null, null)
}


object CommentRepository {
    init {

    }
    fun create(evaluatorRef: Int, applicationRef: Int, comment: String) {
        var comment = Comments(null, evaluatorRef, applicationRef, comment);
        DB.crudSave("Comments", Comments::class, comment, null)
    }
    fun read(pattern: Comments): ResultSet {
        return DB.crudRead("Comments", Comments::class, pattern)

    }
    fun update(id: Int, evaluatorRef: Int, applicationRef: Int, comment: String) {
        var comment = Comments(id, evaluatorRef, applicationRef, comment);
        DB.crudSave("Comments", Comments::class, comment, id)
    }
    fun delete(id: Int) {
        DB.crudDelete("Comments", id)
    }

}
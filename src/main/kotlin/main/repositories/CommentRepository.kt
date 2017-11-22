package main.repositories;

import java.sql.ResultSet

data class Comments(var id: Int?, var userref: Int?, var jobref: Int?, var comment: String?) {
    constructor() : this(null, null, null, null)
}


object CommentRepository {
    init {

    }
    fun create(userRef: Int, jobRef: Int, comment: String) {
        var comment = Comments(null, userRef, jobRef, comment);
        DB.crudSave("Comments", Comments::class, comment, null)
    }
    fun read(pattern: Comments): ResultSet {
        return DB.crudRead("Comments", Comments::class, pattern)

    }
    fun update(id: Int, userRef: Int, jobRef: Int, comment: String) {
        var comment = Comments(id, userRef, jobRef, comment);
        DB.crudSave("Comments", Comments::class, comment, id)
    }
    fun delete(id: Int) {
        DB.crudDelete("Comments", id)
    }

}
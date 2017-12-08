package main.repositories;

import java.sql.ResultSet

data class Evaluations(var id: Int?, var set: String?, var question: String?,
                       var userref: Int?, var jobref: Int?) {
    constructor() : this(null, null, null, null, null)
}


object EvaluationRepository {
    init {
    }
    fun create(set: String, question: String, userRef: Int, jobRef: Int) {
        var evaluation = Evaluations(null, set, question, userRef, jobRef);
        DB.crudSave("evaluations", Evaluations::class, evaluation, null)
    }
    fun read(pattern: Evaluations): ResultSet {
        return DB.crudRead("evaluations", Evaluations::class, pattern)

    }
    fun update(id: Int, set: String?, question: String?, userRef: Int?, jobRef: Int?) {
        var evaluation = Evaluations(id, set, question, userRef, jobRef)
        DB.crudSave("evaluations", Evaluations::class, evaluation, id)
    }
    fun delete(id: Int) {
        DB.crudDelete("evaluations", id)
    }
    fun readDistinct(pattern: Evaluations): ResultSet {
        return DB.crudRead("evaluations", Evaluations::class, pattern, subset="set, question")
    }

}
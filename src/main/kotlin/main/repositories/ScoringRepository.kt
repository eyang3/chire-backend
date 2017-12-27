package main.repositories
import java.sql.ResultSet

data class Scorings(var id: Int?, var hrref: Int?, var evaluatorref: Int?,
                    var candidateref: Int?, var jobref: Int?, var score: Int?) {
    constructor() : this(null, null, null, null, null, null)
}


object ScoringRepository {
    init {

    }
    fun create(hrref: Int, evaluatorref: Int,
               candidateref: Int, jobref: Int, score: Int) {
        var evaluation = Scorings(null, hrref, evaluatorref, candidateref, jobref, score);
        DB.crudSave("scorings", Scorings::class, evaluation, null)
    }

    fun read(pattern: Scorings): ResultSet {
        return DB.crudRead("scorings", Scorings::class, pattern)
    }

    fun update(id: Int, hrref: Int?, evaluatorref: Int?,
               candidateref: Int?, jobref: Int?, score: Int?) {
        var scoring = Scorings(id, hrref, evaluatorref, candidateref, jobref, score);
        DB.crudSave("scorings", Scorings::class, scoring, id)
    }

    fun delete(id: Int) {
        DB.crudDelete("scorings", id)
    }

}
package repositories


import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import main.repositories.DB
import java.sql.Date
import java.sql.ResultSet

data class Evaluation(var id: Int?, var applicationref: Int?, var evaluatorref: Int? , var jobref: Int?, var score: Int?) {
    constructor() : this(null, null, null, null, null)
}





object EvaluationRepository: Repository() {
    init {

    }
    override val table: String = "evaluation"


}
package repositories

import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import main.repositories.Contact
import main.repositories.DB
import main.repositories.ContactRepository
import java.sql.Date
import java.sql.ResultSet
import repositories.Repository

data class EvalRequest(var id: Int?, var evaluatorref: Int?, var jobref: Int?, var sent: Boolean?) {
    constructor() : this(null, null, null, null)
}



object EvalRequestRepository : Repository() {
    override val table: String = "evalrequest"
    init {

    }
    fun create(evaluatorRef: Int, jobref: Int, sent: Boolean?) {
        var eval = EvalRequest(null, evaluatorRef, jobref, sent);
        DB.crudSave("evalrequest", EvalRequest::class, eval, null)
    }

    fun update(evaluatorRef: Int?, jobref: Int?, sent: Boolean?) {
        var eval = EvalRequest(null, evaluatorRef, jobref, sent);
        DB.crudSave("evalrequest", EvalRequest::class, eval, null);
    }

}


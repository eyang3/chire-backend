package routes

import main.repositories.*
import org.apache.poi.xwpf.converter.pdf.PdfConverter
import org.apache.poi.xwpf.usermodel.XWPFDocument
import repositories.EvalRequest
import repositories.EvalRequestRepository
import spark.Spark
import java.sql.Date


data class Applications(var id: Int?, var jobref: Int?, var applicantref: Int?,
                        var hrref: Int?, var resumepath: String?, var coverletterpath: String?,
                        var interview: Date?, var offer: Boolean?, var rejection_reason: String?, var
                        eeoc_race: String?, var eeoc_gender: String?, var covername: String?, var resumename: String?) {
    constructor() : this(null, null, null, null, null, null, null, null, null, null, null, null, null)
}


fun EvalRequestRoutes() {
    Spark.post("/ar/evalrequest/:id", { req, res ->
        var request: List<Int> = gson.fromJson(req.body(), Array<Int>::class.java).toList()
        var id: Int = req.params("id").toInt();

        for(i in request) {
            try {
                EvalRequestRepository.create(i, id, false)
                // Have some code to send an email
            } catch(e: Exception) {
                println(e);
                //if there's a resend all flag
            }
        }
        return@post (RESTStatusMessage("success", "jobs", ""));
    }, { gson.toJson(it) })
    Spark.get("/ar/evalrequest/:id", { req, res ->
        var jobRef: Int = req.params("id").toInt();
        var pattern = EvalRequest(null, null, jobRef, null);
        var resultSet = EvalRequestRepository.read(pattern)
        val results = DB.getResults(resultSet, EvalRequest::class)
        return@get (results)
    }, { gson.toJson(it) })

}


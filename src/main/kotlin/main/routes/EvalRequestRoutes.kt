package routes

import main.repositories.*
import org.apache.poi.xwpf.converter.pdf.PdfConverter
import org.apache.poi.xwpf.usermodel.XWPFDocument
import repositories.EvalRequest
import repositories.EvalRequestRepository
import spark.Spark


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
        println(results);
        return@get (results)
    }, { gson.toJson(it) })

}


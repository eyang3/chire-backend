package routes


import main.repositories.ApplicationRepository
import main.repositories.DB
import main.repositories.User
import repositories.EvalRequest
import spark.Spark
import java.nio.file.Files
import java.nio.file.Paths

//title, body, resumepath, coverletterpath,

data class NextJob(var title: String?, var body: String?, var salary: String?, var resumepath: String? , var coverletterpath: String?) {
    constructor() : this(null, null, null, null, null)
}



fun EvaluateRoutes() {
    Spark.get("/ar/getfile/:jobref", { req, res ->
        val bytes = Files.readAllBytes(Paths.get("/home/eyang/files/8996189418160497279.pdf"))
        val raw = res.raw();
        raw.outputStream.write(bytes)
        raw.outputStream.flush()
        raw.outputStream.close()
        res.raw()
    })

    Spark.get("/ar/next2Evaluate/:jobref", { req, res ->
        val jwt = req.cookie("auth")
        var user: User = readJWT(jwt)!!;
        var jobRef: Int = req.params("jobref").toInt();
        try {
            var resultSet = ApplicationRepository.nextApplication(jobRef, user!!.id!!);
            val results = DB.getResults(resultSet, NextJob::class)
            return@get (results)
        } catch(e: Exception) {
            println(e)
        }
        return@get(null)

    }, { gson.toJson(it) })
    Spark.get("/ar/evaluated/:jobref", { req, res ->

    })
    Spark.post("/ar/evaluate/:id", { req, res ->

    })
}
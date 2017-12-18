package routes

import spark.Spark.*
import main.repositories.DB
import main.repositories.JobRepository
import main.repositories.Jobs
import main.repositories.User


fun JobRoutes() {
    get("/ar/ListMyJobs/:userref", {req, res ->
        var check: String = req.params("userref");
        var pattern = Jobs(null, null, null, check.toInt(), null);
        var resultSet = JobRepository.read(pattern)
        val results = DB.getResults(resultSet, Jobs::class)
        return@get(results)
    }, {gson.toJson(it)})

    get("/ar/job/:id", {req, res ->
        var id: String = req.params("id");
        var pattern = Jobs(id.toInt(), null, null, null, null);
        var resultSet = JobRepository.read(pattern)
        val results = DB.getResults(resultSet, Jobs::class)
        if(results.size > 0) {
            return@get(results[0])
        } else {
            return@get(null);
        }
    }, {gson.toJson(it)})

    put("/ar/job/:id", {req, res ->
        var check: String = req.params("id");
        var request: Jobs = gson.fromJson(req.body(), Jobs::class.java)
        try {
            var resultSet = JobRepository.update(check.toInt(), request.title, request.salary, request.userref, request.body)
            return@put(RESTStatusMessage("success", "jobs", "{\"id\": $check}"))
        }catch(e: Exception) {
            return@put(RESTStatusMessage("error", "jobs", "Unable to update job"))
        }

    }, {gson.toJson(it)});

    delete("/ar/job/:id", {req, res ->
        var id: String = req.params("id");
        var resultSet = JobRepository.delete(id.toInt())
        return@delete(RESTStatusMessage("success", "jobs", ""))

    })

    put("/ar/job", {req, res->
        val jwt = req.cookie("auth")
        var user: User = readJWT(jwt)!!;
        var request: Jobs = gson.fromJson(req.body(), Jobs::class.java)
        try {
            var request: Jobs = gson.fromJson(req.body(), Jobs::class.java)
            var resultSet = JobRepository.create(request.title!!, request.salary!!, user.id!!, request.body!!)
            println(resultSet);
            return@put(RESTStatusMessage("success", "jobs", "{\"id\": $resultSet}"))
        }catch(e: Exception) {
            println(e);
            return@put(RESTStatusMessage("error", "jobs", "Unable to create job"))
        }
    },{gson.toJson(it)})
}

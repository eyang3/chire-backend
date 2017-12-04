package routes

import spark.Spark.*
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import main.repositories.DB
import main.repositories.JobRepository
import main.repositories.Jobs

fun JobRoutes() {
    get("ListMyJobs/:userref", {req, res ->
        var check: String = req.params("userref");
        var pattern = Jobs(null, null, null, check.toInt(), null);
        var resultSet = JobRepository.read(pattern)
        val results = DB.getResults(resultSet, Jobs::class)
        return@get(results)

    }, {gson.toJson(it)})

    get("job/:id", {req, res ->
        var check: String = req.params("id");
        var pattern = Jobs(check.toInt(), null, null, null, null);
        var resultSet = JobRepository.read(pattern)
        val results = DB.getResults(resultSet, Jobs::class)
        return@get(results)
    }, {gson.toJson(it)});

    post("job/:id", {req, res ->
        /*var check: String = req.params("id");
        var pattern = Jobs(check.toInt(), null, null, null, null);
        var resultSet = JobRepository.read(pattern)
        val results = DB.getResults(resultSet, Jobs::class)*/

    }, {gson.toJson(it)});

    delete("job/:id", {req, res ->

    })

    put("job", {req, res->

    })


}

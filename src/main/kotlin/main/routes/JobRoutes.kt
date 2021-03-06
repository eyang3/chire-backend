package routes

import com.google.gson.annotations.SerializedName
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import main.repositories.DB
import main.repositories.JobRepository
import main.repositories.Jobs
import main.repositories.User
import spark.Spark.*

data class idList(@SerializedName("ids") val ids: Array<Int>)


data class JobResult(var pages: List<Jobs>?, var totalRecords: Int?) {
    constructor() : this(null, null)
}


fun encryptRouteInstruction(id: Int, action: String): String {
    var roleClaims = mutableMapOf<String, Any>()
    roleClaims["jobRef"] = id
    roleClaims["action"] = action
    var token: String = Jwts.builder()
            .setClaims(roleClaims.toMap())
            .signWith(SignatureAlgorithm.HS512, "HelloWorld")
            .compact()
    return (token)
}

fun JobRoutes() {
    get("/ar/applicationLink/:id", { req, res ->
        var id: Int = req.params("id") as Int;
        var token = encryptRouteInstruction(id, "apply")
        return@get (RESTStatusMessage("success", "jobs", "{\"token\": $token}"));
    }, { gson.toJson(it) })

    put("/ar/job", { req, res ->
        val jwt = req.cookie("auth")
        var user: User = readJWT(jwt)!!;
        var request: Jobs = gson.fromJson(req.body(), Jobs::class.java)
        try {
            var request: Jobs = gson.fromJson(req.body(), Jobs::class.java)
            var resultSet = JobRepository.create(request.title!!, request.salary, user.id!!, request.body,
                    request.keywords, request.category)
            return@put (RESTStatusMessage("success", "jobs", "{\"id\": $resultSet}"))
        } catch (e: Exception) {
            println(e);
            return@put (RESTStatusMessage("error", "jobs", "Unable to create job"))
        }

    }, { gson.toJson(it) })

    get("/ar/ListMyJobs", { req, res ->
        val jwt = req.cookie("auth")
        try {
            var subset = "jobs.id,title,category,keywords,applications.last_modified";
            var pagingParams = extractPagingParams(req, subset)

            var user: User = readJWT(jwt)!!;
            var pattern = Jobs(null, null, null, user.id,
                    null, null, null, null, null);

            var resultSet = JobRepository.read(pattern, subset = "id,title,category,keywords,last_modified",
                    limit = pagingParams.limit, offset = pagingParams.offset, freeText = pagingParams.textQuery,
                    sortBy = pagingParams.sortBy, dir = pagingParams.dir)
            val results = DB.getResults(resultSet, Jobs::class, subset = "id,title,category,keywords,last_modified")
            val resultCount = JobRepository.totalRecords(pattern, subset = "id,title,category,keywords,last_modified",
                    freeText = pagingParams.textQuery)
            val retVal = JobResult(pages = results, totalRecords = resultCount)
            return@get (retVal)
        } catch (e: Exception) {
            println(e.printStackTrace())
        }
        return@get (null);
    }, { gson.toJson(it) })

    get("/ar/job/:id", { req, res ->
        var id: String = req.params("id");
        var pattern = Jobs(id.toInt(), null, null, null, null, null, null, null, null);
        var resultSet = JobRepository.read(pattern)
        val results = DB.getResults(resultSet, Jobs::class)
        if (results.size > 0) {
            return@get (results[0])
        } else {
            return@get (null);
        }
    }, { gson.toJson(it) })

    put("/ar/job/:id", { req, res ->
        var check: String = req.params("id");
        var request: Jobs = gson.fromJson(req.body(), Jobs::class.java)
        try {
            JobRepository.update(check.toInt(), request.title, request.salary, request.userref, request.body,
                    request.keywords, request.category)
            return@put (RESTStatusMessage("success", "jobs", "{\"id\": $check}"))
        } catch (e: Exception) {
            return@put (RESTStatusMessage("error", "jobs", "Unable to update job"))
        }
    }, { gson.toJson(it) });

    delete("/ar/job", { req, res ->
        try {
            var request: idList = gson.fromJson(req.body(), idList::class.java)
            JobRepository.bulkDelete(request.ids.toList())
        } catch (e: Exception) {
            println(e)
        }
        return@delete (RESTStatusMessage("success", "jobs", ""))
    })

    put("/ar/job", { req, res ->
        val jwt = req.cookie("auth")
        var user: User = readJWT(jwt)!!;
        var request: Jobs = gson.fromJson(req.body(), Jobs::class.java)
        try {
            var request: Jobs = gson.fromJson(req.body(), Jobs::class.java)
            var resultSet = JobRepository.create(request.title!!, request.salary, user.id!!, request.body,
                    request.keywords, request.category)
            return@put (RESTStatusMessage("success", "jobs", "{\"id\": $resultSet}"))
        } catch (e: Exception) {
            println(e);
            return@put (RESTStatusMessage("error", "jobs", "Unable to create job"))
        }
    }, { gson.toJson(it) })

    post("/ar/job/getLink", { req, res ->

    })
}

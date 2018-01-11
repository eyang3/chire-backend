package routes

import main.repositories.DB
import main.repositories.JobRepository
import main.repositories.Jobs
import main.repositories.User
import spark.Spark

fun ApplicantRoutes() {

    Spark.get("/ar/ListMyApplications", { req, res ->
        val jwt = req.cookie("auth")
        try {
            var pageSize = req.queryParams("pageSize")?.toInt();
            var page = req.queryParams("page")?.toInt();
            var textQuery: String? = req.queryParams("freeText");
            var sortBy: String? = req.queryParams("sortBy");
            if (sortBy == null) {
                sortBy = ""
            }
            if ("title,category,keywords,last_modified".indexOf(sortBy) == -1) {
                println("error");
                sortBy = ""
            }

            var dir: String? = req.queryParams("dir");

            if (dir != "ASC" && dir != "DESC") {
                dir = ""
            }

            if (textQuery == null) {
                textQuery = "";
            }
            if (pageSize == null) {
                pageSize = 100;
            }
            if (page == null) {
                page = 1;
            }
            var limit = pageSize.toString()
            var offset = ((page - 1) * pageSize).toString()
            var subset = "jobs.id,title,category,keywords,applications.last_modified";
            var user: User = readJWT(jwt)!!;
            var resultSet = JobRepository.jobsFromApplication(user.id!!, subset = subset,
                    limit = limit, offset = offset, freeText = textQuery, sortBy = sortBy, dir = dir)
            val results = DB.getResults(resultSet, Jobs::class, subset = subset)
            val resultCount = JobRepository.countJobsFromApplication(user.id!!, freeText = textQuery)
            val retVal = JobResult(pages = results, totalRecords = resultCount)
            return@get (retVal)
        } catch (e: Exception) {
            println(e.printStackTrace())
        }
        return@get (null);
    }, { gson.toJson(it) })
}

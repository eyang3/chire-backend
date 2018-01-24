package routes

import main.repositories.DB
import main.repositories.JobRepository
import main.repositories.Jobs
import main.repositories.User
import spark.Spark
import java.io.File
import java.nio.charset.Charset
import java.nio.file.Files
import java.nio.file.StandardCopyOption
import javax.servlet.MultipartConfigElement


fun ApplicantRoutes() {
    Spark.put("/ar/apply/:id", { req, res ->

    })
    Spark.post("/ar/testFile", { req, res ->
        val uploadDir = File("~/files")
        uploadDir.mkdir()
        req.attribute("org.eclipse.jetty.multipartConfig", MultipartConfigElement("/tmp"))
        try {
            val gender = req.raw().getPart("gender").inputStream.use { input ->
                var gender = input.readBytes().toString(Charset.defaultCharset())
                return@use (gender);
            }
            val race = req.raw().getPart("race").inputStream.use { input ->
                var race = input.readBytes().toString(Charset.defaultCharset())
                return@use (race);
            }

        } catch (e: Exception) {
            println(e)
        }

        var tempFile = Files.createTempFile(uploadDir.toPath(), "", "")

        req.raw().getPart("resume").inputStream.use { input ->
            Files.copy(input, tempFile, StandardCopyOption.REPLACE_EXISTING)
        }
        req.raw().getPart("cover").inputStream.use { input ->
            tempFile = Files.createTempFile(uploadDir.toPath(), "", "")
            Files.copy(input, tempFile, StandardCopyOption.REPLACE_EXISTING)
        }
        return@post (null);
    }, { gson.toJson(it) })
    Spark.get("/ar/ListMyApplications", { req, res ->
        val jwt = req.cookie("auth")
        try {
            var subset = "jobs.id,title,category,keywords,applications.last_modified";
            var pagingParams = extractPagingParams(req, subset)
            var user: User = readJWT(jwt)!!;
            var resultSet = JobRepository.jobsFromApplication(user.id!!, subset = subset,
                    limit = pagingParams.limit, offset = pagingParams.offset, freeText = pagingParams.textQuery,
                    sortBy = pagingParams.sortBy, dir = pagingParams.dir)
            val results = DB.getResults(resultSet, Jobs::class, subset = subset)
            val resultCount = JobRepository.countJobsFromApplication(user.id!!, freeText = pagingParams.textQuery)
            val retVal = JobResult(pages = results, totalRecords = resultCount)
            return@get (retVal)
        } catch (e: Exception) {
            println(e.printStackTrace())
        }
        return@get (null);
    }, { gson.toJson(it) })
}

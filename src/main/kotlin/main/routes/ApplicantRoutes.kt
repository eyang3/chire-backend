package routes

import main.repositories.DB
import main.repositories.JobRepository
import main.repositories.Jobs
import main.repositories.User
import spark.Spark
import java.io.*;
import java.nio.file.*;
import java.nio.file.StandardCopyOption
import javax.servlet.MultipartConfigElement
import java.nio.file.Path
import java.io.File





fun ApplicantRoutes() {
    Spark.post("/ar/testFile", {req, res ->

        val uploadDir = File("/tmp")
        println(uploadDir)
        uploadDir.mkdir()
        var tempFile = Files.createTempFile(uploadDir.toPath(), "", "")

        req.attribute("org.eclipse.jetty.multipartConfig", MultipartConfigElement("/tmp"))
        req.raw().getPart("resume").inputStream.use { // getPart needs to use same "name" as input field in form
            input ->
            println(input);
            Files.copy(input, tempFile, StandardCopyOption.REPLACE_EXISTING)
        }

        req.raw().getPart("cover").inputStream.use { // getPart needs to use same "name" as input field in form
            input ->
            println(input)
            tempFile = Files.createTempFile(uploadDir.toPath(), "", "")
            Files.copy(input, tempFile, StandardCopyOption.REPLACE_EXISTING)
        }
        return@post(null);
    },  { gson.toJson(it) })
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

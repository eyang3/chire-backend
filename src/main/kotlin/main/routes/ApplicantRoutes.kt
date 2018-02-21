package routes

import main.repositories.DB
import main.repositories.JobRepository
import main.repositories.Jobs
import main.repositories.User
import main.repositories.ApplicationRepository
import main.repositories.Applications
import spark.Spark
import java.io.File
import java.nio.charset.Charset
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption
import javax.servlet.MultipartConfigElement
import org.apache.poi.xwpf.converter.pdf.PdfConverter;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import java.io.FileOutputStream
import java.io.InputStream


data class SingleApplication(var title: String?, var body: String?, var eeoc_race:String?, var eeoc_gender:String?,
                             var resumename: String?, var covername: String?) {
    constructor() : this(null, null, null, null, null, null)
}


fun ApplicantRoutes() {
    Spark.post("/ar/apply/:id", { req, res ->
        val uploadDir = File("/home/eyang/files")
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
            val resumeFile: Path? = req.raw().getPart("resume").inputStream.use { input ->
                val originalName = req.raw().getPart("resume").submittedFileName;
                println(originalName);
                return@use path(input, originalName, uploadDir)

            }
            val coverFile: Path? = req.raw().getPart("cover").inputStream.use { input ->
                val originalName = req.raw().getPart("cover").submittedFileName;
                return@use path(input, originalName, uploadDir)
            }
            val jwt = req.cookie("auth")
            var user: User = readJWT(jwt)!!;
            var id: String = req.params("id")
            println(race)
            println(gender)
            var applicationResultSet = ApplicationRepository.read(Applications(null, id.toInt(), user.id, null, null,
                    null, null, null, null, null, null, null, null))
            var applicationResult = DB.getResults(applicationResultSet, Applications::class);
            ApplicationRepository.update(applicationResult[0]!!.id!!, null, null, null,
                    resumeFile.toString(), coverFile.toString(), null,  null, null, race, gender,
                    req.raw().getPart("cover").submittedFileName, req.raw().getPart("resume").submittedFileName);
        } catch (e: Exception) {
            println(e)
        }
        return@post (RESTStatusMessage("success", "jobs", ""));
    }, { gson.toJson(it) })

    Spark.get("/ar/JobApplication/:id", {req, res->
        val jwt = req.cookie("auth")
        var user: User = readJWT(jwt)!!;
        var id: String = req.params("id")
        var resultSet = ApplicationRepository.getApplicationPageInfo(id.toInt(), user.id!!);
        var results = DB.getResults(resultSet,SingleApplication::class)
        return@get (results)
    }, { gson.toJson(it)} )

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

private fun path(input: InputStream?, originalName: String?, uploadDir: File): Path? {
    var stream = input;
    if(originalName == null) {
        return null;
    }
    if (originalName.indexOf(".doc") != -1) {
        var outFile = Files.createTempFile(uploadDir.toPath(), "", ".pdf")
        var out = FileOutputStream(outFile.toFile())
        try {
            val doc = XWPFDocument(input);
            PdfConverter.getInstance().convert(doc, out, null);
        } catch (e: Exception) {
            println(e)
        }
        return (outFile)
    } else {
        var tempFile = Files.createTempFile(uploadDir.toPath(), "", ".pdf")
        Files.copy(stream, tempFile, StandardCopyOption.REPLACE_EXISTING)
        return (tempFile)
    }
}

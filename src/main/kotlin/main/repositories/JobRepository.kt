package main.repositories

import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import java.sql.Date
import java.sql.ResultSet
import repositories.Repository

data class Jobs(var id: Int?, var title: String?, var salary: String?,
                var userref: Int?, var body: String?, var keywords: String?,
                var category: String?, var last_modified: Date?, var link: String?) {
    constructor() : this(null, null, null, null, null, null, null, null, null)
}

fun encryptRouteInstruction(id: Int, user: Int, action: String): String {
    var roleClaims = mutableMapOf<String, Any>()
    roleClaims["jobRef"] = id
    roleClaims["action"] = action
    roleClaims["hrRef"] = user
    var token: String = Jwts.builder()
            .setClaims(roleClaims.toMap())
            .signWith(SignatureAlgorithm.HS512, "HelloWorld")
            .compact()
    return (token)
}

object JobRepository: Repository() {
    init {

    }
    override val table: String = "jobs"
    fun create(title: String, salary: String?, userRef: Int, body: String?, keywords: String?, category: String?): Int {
        var job = Jobs(null, title, salary, userRef, body, keywords, category, null, null)
        var id = DB.crudSave("jobs", Jobs::class, job, null)
        job.link = encryptRouteInstruction(id, userRef, "apply");
        DB.crudSave("jobs", Jobs::class, job, id)
        return (id);
    }

    fun read(pattern: Jobs, subset: String = "", limit: Int = 100,
             offset: Int = 0, freeText: String = "", dir: String = "ASC", sortBy: String = ""): ResultSet {
        return DB.crudRead("jobs", Jobs::class, pattern, subset = subset, limit = limit, offset = offset,
                indexFields = "tsv", freeText = freeText, dir = dir, sortBy = sortBy)
    }


    fun update(id: Int, title: String?, salary: String?, userRef: Int?, body: String?,
               keywords: String?, category: String?) {
        var job = Jobs(id, title, salary, userRef, body, keywords, category, null, null)
        DB.crudSave("jobs", Jobs::class, job, id)
    }

    fun jobsFromApplication(applicantRef: Int, subset: String = "", limit: Int = 100,
                            offset: Int = 0, freeText: String = "", dir: String = "ASC", sortBy: String = ""): ResultSet {
        var connection = DB.connection()
        var sets = ""
        val triple = DB.subsetSort(freeText, sets, "tsv", subset, sortBy, dir)
        var _subset = triple.first
        var _sortBy = triple.second
        sets = triple.third
        var query = """SELECT $_subset FROM jobs join applications on applications.jobref = jobs.id
                       where applicantref = ? $sets $_sortBy LIMIT ? OFFSET ? """
        var statement = connection.prepareStatement(query);
        var count = 1
        statement.setInt(count++, applicantRef)
        if (sets != "") {
            statement.setString(count++, freeText)
        }
        statement.setInt(count++, limit)
        statement.setInt(count++, offset)
        val resultSet = statement.executeQuery();
        connection.close()
        return resultSet;
    }

    fun countJobsFromApplication(applicantRef: Int, freeText: String = ""): Int {
        var connection = DB.connection()
        var sets = ""
        val triple = DB.subsetSort(freeText, sets, "tsv", "", "", "")
        sets = triple.third
        var query = """SELECT count(*) FROM jobs join applications on applications.jobref = jobs.id
                       where applicantref = ? $sets """
        var statement = connection.prepareStatement(query);
        var count = 1
        statement.setInt(count++, applicantRef)
        if (sets != "") {
            statement.setString(count++, freeText)
        }
        val resultSet = statement.executeQuery()
        resultSet.next();
        return resultSet.getInt(1)
    }


}
package main.repositories;

import repositories.Repository
import java.sql.ResultSet
import java.sql.Date


data class Applications(var id: Int?, var jobref: Int?, var applicantref: Int?,
                        var hrref: Int?, var resumepath: String?, var coverletterpath: String?,
                        var interview: Date?) {
    constructor() : this(null, null, null, null, null, null, null)
}


object ApplicationRepository: Repository() {
    override val table: String = "applications"
    init {

    }
    fun create(jobRef: Int, applicantRef: Int, hrRef: Int, resumePath: String, coverletterPath: String) {
        var application = Applications(null, jobRef, applicantRef, hrRef, resumePath, coverletterPath, null);
        DB.crudSave("applications", Applications::class, application, null)
    }

    fun update(id: Int, jobRef: Int, applicantRef: Int, hrRef: Int,
               resumePath: String, coverletterPath: String, date: Date?) {
        var application = Applications(id, jobRef, applicantRef, hrRef, resumePath, coverletterPath, date);
        DB.crudSave("applications", Applications::class, application, id)
    }

}
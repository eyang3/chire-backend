package main.repositories;

import repositories.Repository
import java.sql.ResultSet
import java.sql.Date


data class Applications(var id: Int?, var jobref: Int?, var applicantref: Int?,
                        var hrref: Int?, var resumepath: String?, var coverletterpath: String?,
                        var interview: Date?, var offer: Boolean?, var rejectReason: String?) {
    constructor() : this(null, null, null, null, null, null, null, null, null)
}


object ApplicationRepository: Repository() {
    override val table: String = "applications"
    init {

    }
    fun create(jobRef: Int, applicantRef: Int, hrRef: Int, resumePath: String, coverletterPath: String) {
        try {
            var application = Applications(null, jobRef, applicantRef, hrRef, resumePath, coverletterPath, null, null, null);
            DB.crudSave("applications", Applications::class, application, null)
        } catch(e: Exception) {
            println(e);
        }

    }
    fun update(id: Int, jobRef: Int, applicantRef: Int, hrRef: Int,
               resumePath: String?, coverletterPath: String?, date: Date?,
               offer: Boolean, rejectReason: String?) {
        var application = Applications(id, jobRef, applicantRef,
                hrRef, resumePath, coverletterPath, date, offer, rejectReason);
        DB.crudSave("applications", Applications::class, application, id)
    }

}
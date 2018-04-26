package main.repositories;

import repositories.Repository
import java.sql.ResultSet
import java.sql.Date


data class Applications(var id: Int?, var jobref: Int?, var applicantref: Int?,
                        var hrref: Int?, var resumepath: String?, var coverletterpath: String?,
                        var interview: Date?, var offer: Boolean?, var rejection_reason: String?, var
                        eeoc_race: String?, var eeoc_gender: String?, var covername: String?, var resumename: String?) {
    constructor() : this(null, null, null, null, null, null, null, null, null, null, null, null, null)
}



object ApplicationRepository: Repository() {
    override val table: String = "applications"
    init {

    }
    fun create(jobRef: Int, applicantRef: Int, hrRef: Int, resumePath: String, coverletterPath: String) {
        try {
            var application = Applications(null, jobRef, applicantRef, hrRef, resumePath, coverletterPath,
                    null, null, null, null, null, null, null);
            DB.crudSave("applications", Applications::class, application, null)
        } catch(e: Exception) {
            println(e);
        }

    }
    fun getApplicationPageInfo(jobRef: Int, applicantRef: Int): ResultSet {
        var connection = DB.connection()

        var query = """SELECT title, body, resumename, resumepath, covername, coverletterpath, eeoc_gender, eeoc_race
                       FROM applications join jobs on applications.jobref = jobs.id
                       where applicantref = ? and applications.jobref = ?"""
        var statement = connection.prepareStatement(query);
        var count = 1
        statement.setInt(count++, applicantRef)
        statement.setInt(count++, jobRef)
        val resultSet = statement.executeQuery();
        connection.close()
        return resultSet
    }
    fun nextApplication(jobRef: Int, evaluatorRef: Int) : ResultSet {

            var connection = DB.connection()

            var query = """select title, body, salary, resumepath, coverletterpath, num from
                        (SELECT title, body, resumepath, coverletterpath, count(evaluations.id) as num
                            FROM applications
                            join jobs on applications.jobref = jobs.id
                            join evalrequest on evalrequest.jobref = applications.jobref
                            left join evaluations on evaluations.applicationref = jobs.id
                            where evalrequest.jobref = ? and evalrequest.evaluatorref = ?
                            group by title, body, resumepath, coverletterpath) a order by num limit 1"""
            var statement = connection.prepareStatement(query);
            var count = 1
            statement.setInt(count++, jobRef)
            statement.setInt(count++, evaluatorRef)
            val resultSet = statement.executeQuery();
            connection.close()
            return resultSet



    }

    fun update(id: Int, jobRef: Int?, applicantRef: Int?, hrRef: Int?,
               resumePath: String?, coverletterPath: String?, date: Date?,
               offer: Boolean?, rejectReason: String?, eeoc_race: String?, eeoc_gender: String?,
               covername: String?, resumename: String?) {
        var application = Applications(id, jobRef, applicantRef,
                hrRef, resumePath, coverletterPath, date, offer, rejectReason, eeoc_race, eeoc_gender, covername, resumename);
        DB.crudSave("applications", Applications::class, application, id)
    }

}
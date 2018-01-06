package routes

import main.repositories.Contact
import main.repositories.ContactRepository
import main.repositories.DB
import main.repositories.User
import spark.Spark.get

data class Results<T>(var pages: List<T>?, var totalRecords: Int?) {
    constructor() : this(null, null)
}

fun ContactRoutes() {

    get("/ar/ListMyContacts", { req, res ->
        val jwt = req.cookie("auth")
        try {
            var pageSize = req.queryParams("pageSize")?.toInt();
            var page = req.queryParams("page")?.toInt();
            var textQuery: String? = req.queryParams("freeText");
            var sortBy: String? = req.queryParams("sortBy");
            if (sortBy == null) {
                sortBy = ""
            }
            if ("email,name,label".indexOf(sortBy) == -1) {
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

            var user: User = readJWT(jwt)!!;
            var pattern = Contact(null, user.id, null, null, null);

            var resultSet = ContactRepository.read(pattern, subset = "id,email,name,label",
                    limit = limit, offset = offset, freeText = textQuery, sortBy = sortBy, dir = dir)
            val results = DB.getResults(resultSet, Contact::class, subset = "id,email,name,label")
            val resultCount = ContactRepository.totalRecords(pattern, subset = "id,email,name,label",
                    freeText = textQuery)
            val retVal = Results<Contact>(pages = results, totalRecords = resultCount)
            return@get (retVal)

        } catch (e: Exception) {
            return@get (RESTStatusMessage("error", "contacts", "Unable to search contacts"))
        }
    }, { gson.toJson(it) })

    get("/ar/contact/:id", { req, res ->
        var id: String = req.params("id")
        var pattern = Contact(id.toInt(), null, null, null, null);
        var resultSet = ContactRepository.read(pattern)
        val results = DB.getResults(resultSet, Contact::class)
        if (results.size > 0) {
            return@get (results[0])
        } else {
            return@get (null);
        }
    }, { gson.toJson(it) })
}
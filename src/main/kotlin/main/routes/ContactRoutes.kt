package routes

import main.repositories.*
import spark.Spark
import spark.Spark.delete
import spark.Spark.get

data class Results<T>(var pages: List<T>?, var totalRecords: Int?) {
    constructor() : this(null, null)
}

fun ContactRoutes() {

    get("/ar/ListMyContacts", { req, res ->
        val jwt = req.cookie("auth")
        try {
            var subset = "id, email,name,label,contactref"
            var pagingParams = extractPagingParams(req, subset)
            var user: User = readJWT(jwt)!!;
            var pattern = Contact(null, user.id, null, null, null, null);
            println(pagingParams);
            var resultSet = ContactRepository.read(pattern, subset = subset,
                    limit = pagingParams.limit, offset = pagingParams.offset, freeText = pagingParams.textQuery,
                    sortBy = pagingParams.sortBy, dir = pagingParams.dir)
            val results = DB.getResults(resultSet, Contact::class, subset = subset)
            val resultCount = ContactRepository.totalRecords(pattern, subset = "id,email,name,label",
                    freeText = pagingParams.textQuery)
            val retVal = Results<Contact>(pages = results, totalRecords = resultCount)
            return@get (retVal)

        } catch (e: Exception) {
            return@get (RESTStatusMessage("error", "contacts", "Unable to search contacts"))
        }
    }, { gson.toJson(it) })

    get("/ar/contact/:id", { req, res ->
        var id: String = req.params("id")
        var pattern = Contact(id.toInt(), null, null, null, null, null);
        var resultSet = ContactRepository.read(pattern)
        val results = DB.getResults(resultSet, Contact::class)
        if (results.size > 0) {
            return@get (results[0])
        } else {
            return@get (null)
        }
    }, { gson.toJson(it) })

    delete("/ar/contact", { req, res ->
        try {
            var request: idList = gson.fromJson(req.body(), idList::class.java)
            ContactRepository.bulkDelete(request.ids.toList())
        } catch (e: Exception) {
            println(e)
        }
        return@delete (RESTStatusMessage("success", "jobs", ""))
    })

    Spark.put("/ar/contact", { req, res ->
        val jwt = req.cookie("auth")
        var user: User = readJWT(jwt)!!;
        var request: Contact = gson.fromJson(req.body(), Contact::class.java)
        try {
            var request: Contact = gson.fromJson(req.body(), Contact::class.java)
            try {
                UserRepository.signup(request.email!!, "", 2)
                UserRepository.blankUser(request.email!!)
            } catch (e: Exception) {
            }
            var existingUser = UserRepository.getUser(request.email!!);
            var resultSet = ContactRepository.create(userRef = user.id!!, email = request.email!!, name = request.name,
                    label = request.label, contactref = existingUser[0].id)
            return@put (RESTStatusMessage("success", "contacts", "{\"id\": $resultSet}"))
        } catch (e: Exception) {
            println(request);
            println(e);
            return@put (RESTStatusMessage("error", "contacts", "Unable to create contact"))
        }
    }, { gson.toJson(it) })
}
package routes
import spark.Request
import spark.Spark

data class pagingParams(var limit: Int, var offset: Int, var textQuery: String, var sortBy: String, var dir: String)

fun extractPagingParams(req: Request, validate: String): pagingParams {
    var pageSize = req.queryParams("pageSize")?.toInt();
    var page = req.queryParams("page")?.toInt();
    var textQuery: String? = req.queryParams("freeText");
    var sortBy: String? = req.queryParams("sortBy");
    if (sortBy == null) {
        sortBy = ""
    }
    if (validate.indexOf(sortBy) == -1) {
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

    var limit = pageSize
    var offset = ((page - 1) * pageSize)
    return(pagingParams(limit, offset, textQuery, sortBy, dir))
}
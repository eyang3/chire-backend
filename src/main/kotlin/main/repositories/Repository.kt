package repositories

import main.repositories.DB
import java.sql.ResultSet


open class Repository {
    val table = "NA"

    init {
    }
    inline fun <reified T: Any>read(pattern: T, subset: String = "", limit: String = "100",
             offset: String = "0", freeText: String = "", dir: String="ASC", sortBy: String = ""): ResultSet {
        return DB.crudRead(this.table, T::class, pattern, subset = subset, limit = limit, offset = offset,
                indexFields = "tsv", freeText = freeText, dir=dir, sortBy = sortBy)
    }
    inline fun <reified T: Any> totalRecords(pattern: T, subset: String = "", freeText: String = ""): Int {
        var resultSet = DB.countRows(this.table, T::class, pattern,
                subset = "", freeText = freeText, indexFields = "tsv");
        resultSet.next();
        return (resultSet.getInt(1))
    }
    fun delete(id: Int) {
        DB.crudDelete(this.table, id)
    }
    fun bulkDelete(ids: List<Int>) {
        DB.bulkDelete(this.table, ids)
    }

}
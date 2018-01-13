package main.repositories

import org.postgresql.ds.PGConnectionPoolDataSource
import java.sql.*
import kotlin.reflect.KClass
import kotlin.reflect.KMutableProperty
import kotlin.reflect.full.createInstance
import kotlin.reflect.full.memberProperties


object DB {
    lateinit private var dataSource: PGConnectionPoolDataSource

    init {

    }

    fun connect(url: String, user: String, password: String?) {
        this.dataSource = PGConnectionPoolDataSource()
        this.dataSource.url = url
        this.dataSource.user = user
        this.dataSource.password = password
        this.dataSource.isDefaultAutoCommit = true
    }

    fun connection(): Connection {
        return this.dataSource.connection
    }

    fun <T : Any> crudRead(table: String, entityClass: KClass<T>, obj: T,
                           limit: Int = 100, offset: Int = 0, sortBy: String = "",
                           subset: String = "", distinct: Boolean = false,
                           freeText:
                           String = "", indexFields: String = "", dir: String = "ASC"): ResultSet {


        val conn = this.connection()
        val objInfo = getDataMembers(entityClass)
        var members = objInfo.third
        val types = objInfo.second
        val notNullIndex = members.mapIndexed { index, it ->
            if (it.getter.call(obj) != null) {
                index
            } else {
                null
            }
        }.filter { it -> it != null }
        val fields = members.map { it ->
            if (it.getter.call(obj) == null) {
                null
            } else {
                it.name
            }
        }.filter { it -> it != null }
        var sets = fields.map { it -> "$it = ?" }.joinToString(" AND ")
        val triple = subsetSort(freeText, sets, indexFields, subset, sortBy, dir)
        var _subset = triple.first
        var _sortBy = triple.second
        sets = triple.third
        var query = "SELECT $_subset FROM $table"
        if (fields.size > 0) {
            query = "SELECT $_subset FROM $table where $sets $_sortBy LIMIT ? OFFSET ? "
        }
        if (distinct != null && distinct == true) {
            query = "SELECT DISTINCT $_subset FROM $table where $sets $_sortBy LIMIT ? OFFSET ?"
        }

        var statement = conn.prepareStatement(query);
        var current = fieldSetter(notNullIndex, types, statement, members, obj, conn)
        current++;
        if (freeText != "") {
            statement.setString(current++, freeText);
        }
        statement.setInt(current++, limit)
        statement.setInt(current++, offset)


        val resultSet = statement.executeQuery();
        conn.close()
        return resultSet;
    }

    fun subsetSort(freeText: String, sets: String, indexFields: String, subset: String, sortBy: String, dir: String): Triple<String, String, String> {
        var sets1 = sets
        if (freeText != "") {
            sets1 = sets1 + " AND $indexFields @@ phraseto_tsquery(?) ";
        }
        var _subset = subset
        if (subset == "") {
            _subset = "*"
        }

        var _sortBy = ""
        if (sortBy != "") {
            _sortBy = " ORDER BY $sortBy $dir "
        }
        return Triple(_subset, _sortBy, sets1)
    }

    fun <T : Any> countRows(table: String, entityClass: KClass<T>, obj: T,
                            subset: String = "", distinct: Boolean = false,
                            freeText: String = "", indexFields: String = ""): ResultSet {
        val conn = this.connection()
        val objInfo = getDataMembers(entityClass)
        var members = objInfo.third
        val types = objInfo.second
        val notNullIndex = members.mapIndexed { index, it ->
            if (it.getter.call(obj) != null) {
                index
            } else {
                null
            }
        }.filter { it -> it != null }
        val fields = members.map { it ->
            if (it.getter.call(obj) == null) {
                null
            } else {
                it.name
            }
        }.filter { it -> it != null }
        var sets = fields.map { it -> "$it = ?" }.joinToString(" AND ")
        if (freeText != "") {
            sets = sets + " AND $indexFields @@ phraseto_tsquery(?) ";
        }
        var _subset = subset;
        if (subset == "") {
            _subset = "*"
        }
        var query = "SELECT COUNT(*) FROM $table"
        if (fields.size > 0) {
            query = "SELECT COUNT(*) FROM $table where $sets"
        }
        if (distinct != null && distinct == true) {
            query = "SELECT COUNT(DISTINCT($_subset)) FROM $table where $sets"
        }

        var statement = conn.prepareStatement(query);
        var current = fieldSetter(notNullIndex, types, statement, members, obj, conn)
        current++;
        if (freeText != "") {
            statement.setString(current++, freeText);
        }
        println(statement)
        val resultSet = statement.executeQuery();
        conn.close()
        return resultSet;
    }

    fun bulkDelete(table: String, ids: List<Int>) {
        val conn = this.connection()
        val IdFields = ids.map { id -> "?" }.joinToString(",");
        var statement = conn.prepareStatement("DELETE FROM $table where id in ($IdFields)");
        ids.forEachIndexed { index, i -> statement.setInt(index + 1, ids[index]) }
        statement.execute()
        conn.close()

    }

    fun crudDelete(table: String, id: Int) {
        val conn = this.connection()
        var statement = conn.prepareStatement("DELETE FROM $table where id = ?");
        statement.setInt(1, id);
        statement.execute();
        conn.close()
    }

    fun <T : Any> crudSave(table: String, entityClass: KClass<T>, obj: T, id: Int?): Int {
        val objInfo = getDataMembers(entityClass)
        var members = objInfo.third
        val types = objInfo.second
        val notNullIndex = members.mapIndexed { index, it ->
            if (it.getter.call(obj) != null && it.name != "id") {
                index
            } else {
                null
            }
        }.filter { it -> it != null }

        val fields = members.map { it ->
            if (it.getter.call(obj) == null || it.name == "id") {
                null
            } else {
                it.name
            }
        }.filter { it -> it != null }


        var query = ""
        if (id != null) {
            var sets = fields.map { it -> "$it = ?" }.joinToString(",")
            query = "UPDATE $table set $sets WHERE id = ? RETURNING id";
        } else {
            val cols = fields.joinToString(",")
            val question = fields.map { _ -> "?" }.joinToString(",")
            query = "INSERT INTO $table($cols) VALUES ($question) RETURNING id";
        }
        val conn = this.connection()
        var statement = conn.prepareStatement(query);
        fieldSetter(notNullIndex, types, statement, members, obj, conn)
        if (id != null) {
            statement.setInt(notNullIndex.size + 1, id)
        }
        statement.executeQuery()
        val last_updated_id = statement.getResultSet()
        last_updated_id.next();
        val m = last_updated_id.getInt(1);
        conn.close()
        return m;
    }

    private fun <T : Any> fieldSetter(notNullIndex: List<Int?>, types: List<String>,
                                      statement: PreparedStatement, members: List<KMutableProperty<*>>,
                                      obj: T, conn: Connection): Int {
        var fieldCount = 0
        for (i in notNullIndex) {
            when (types[i!!]) {
                "kotlin.Int?" -> statement.setInt(fieldCount + 1, members[i].getter.call(obj) as Int)
                "kotlin.ByteArray?" -> statement.setBytes(fieldCount + 1, members[i].getter.call(obj) as ByteArray)
                "kotlin.Double?" -> statement.setDouble(fieldCount + 1, members[i].getter.call(obj) as Double)
                "kotlin.Float?" -> statement.setFloat(fieldCount + 1, members[i].getter.call(obj) as Float)
                "kotlin.Long?" -> statement.setLong(fieldCount + 1, members[i].getter.call(obj) as Long)
                "kotlin.String?" -> statement.setString(fieldCount + 1, members[i].getter.call(obj) as String)
                "kotlin.collections.MutableList<kotlin.Int>?" -> {
                    var array = conn.createArrayOf("INTEGER", (members[i].getter.call(obj) as List<Int>).toTypedArray())
                    statement.setArray(fieldCount + 1, array)
                }
                "java.sql.Timestamp?" -> statement.setTimestamp(fieldCount + 1, members[i].getter.call(obj) as Timestamp)
                "java.sql.Date?" -> statement.setDate(fieldCount + 1, members[i].getter.call(obj) as Date)
            }
            fieldCount++
        }
        return (fieldCount);
    }

    private fun <T : Any> getDataMembers(entityClass: KClass<T>): Triple<List<String>, List<String>, List<KMutableProperty<*>>> {
        var retVal = mutableListOf<T>()
        var entity = entityClass.createInstance()
        var members = entity.javaClass.kotlin.memberProperties as List<KMutableProperty<*>>
        var fields = members.map { it -> it.name }.toList();
        var types = members.map { it -> it.returnType.toString() }.toList()
        return Triple<List<String>, List<String>, List<KMutableProperty<*>>>(fields, types, members)
    }

    fun <T : Any> getResults(rs: ResultSet, entityClass: KClass<T>, subset: String? = null): List<T> {
        var retVal = mutableListOf<T>()
        var entity = entityClass.createInstance()
        var members = entity.javaClass.kotlin.memberProperties as List<KMutableProperty<*>>
        if (subset != null) {
            members = members.filter { it ->
                if (subset.indexOf(it.name) != -1) {
                    return@filter (true)
                }
                return@filter (false)
            }
        }
        var fields = members.map { it -> it.name }.toList()
        var types = members.map { it -> it.returnType.toString() }.toList()

        while (rs.next()) {
            var temp = entityClass.createInstance()
            for (i in 0..fields.size - 1) {
                var fieldValue: Any? = null
                try {
                    fieldValue = when (types[i]) {
                        "kotlin.Int?" -> rs.getInt(fields[i])
                        "kotlin.ByteArray?" -> rs.getBytes(fields[i])
                        "kotlin.Double?" -> rs.getDouble(fields[i])
                        "kotlin.Float?" -> rs.getFloat(fields[i])
                        "kotlin.Long?" -> rs.getLong(fields[i])
                        "kotlin.String?" -> rs.getString(fields[i])
                        "kotlin.collections.List<kotlin.Int>?" ->
                            (rs.getArray(fields[i]).array as Array<out Any>).toList() as List<T>
                        "java.sql.Timestamp?" -> rs.getTimestamp(fields[i])
                        "java.sql.Date?" -> rs.getDate(fields[i])
                        else -> null
                    }
                } catch (e: Exception) {
                    println(e)
                }
                members[i].setter.call(temp, fieldValue)
            }
            retVal.add(temp)
        }
        return retVal
    }
}

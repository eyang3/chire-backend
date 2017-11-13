package main.repositories

import org.postgresql.ds.PGPooledConnection
import java.sql.*
import kotlin.reflect.KClass
import kotlin.reflect.KMutableProperty
import kotlin.reflect.full.createInstance
import kotlin.reflect.full.memberProperties


object DB {
    lateinit private var dataSource: PGPooledConnection

    init {

    }

    fun connect(url: String, user: String, password: String?) {
        val con = DriverManager.getConnection(url, user, password)
        this.dataSource = PGPooledConnection(con, true)
    }

    fun connection(): Connection {
        return this.dataSource.connection
    }

    fun <T : Any> crudRead(table: String, entityClass: KClass<T>, obj: T,
                           limit: String = "", offset: String = "", sortBy: String = "",
                           subset: String = ""): ResultSet {
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

        var sets = fields.map { it -> "$it = ?" }.joinToString(",")
        var query = "SELECT * FROM $table"
        if(fields.size > 0) {
            query = "SELECT * FROM $table where $sets $limit $offset $sortBy"
        }
        if(subset != "") {
            query = "SELECT DISTINCT $subset FROM $table where $sets $limit $offset $sortBy"
        }
        println(query);
        var statement = conn.prepareStatement(query);
        fieldSetter(notNullIndex, types, statement, members, obj, conn)
        val resultSet = statement.executeQuery();
        conn.close()
        return resultSet;
    }

    fun crudDelete(table: String, id: Int) {
        val conn = this.connection()
        var statement = conn.prepareStatement("DELETE FROM $table where id = ?");
        statement.setInt(1, id);
        statement.execute();
        conn.close()
    }

    fun <T : Any> crudSave(table: String, entityClass: KClass<T>, obj: T, id: Int?) {
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
            query = "UPDATE $table set $sets WHERE id = ?";
        } else {
            val cols = fields.joinToString(",")
            val question = fields.map { _ -> "?" }.joinToString(",")
            query = "INSERT INTO $table($cols) VALUES ($question)";
        }
        val conn = this.connection()
        var statement = conn.prepareStatement(query);
        fieldSetter(notNullIndex, types, statement, members, obj, conn)
        if (id != null) {
            statement.setInt(notNullIndex.size + 1, id)
        }
        statement.executeUpdate()
        conn.close()
    }

    private fun <T : Any> fieldSetter(notNullIndex: List<Int?>, types: List<String>,
                                      statement: PreparedStatement, members: List<KMutableProperty<*>>,
                                      obj: T, conn: Connection) {
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
                else -> null
            }
            fieldCount++
        }
    }


    private fun <T : Any> getDataMembers(entityClass: KClass<T>): Triple<List<String>, List<String>, List<KMutableProperty<*>>> {
        var retVal = mutableListOf<T>()
        var entity = entityClass.createInstance()
        var members = entity.javaClass.kotlin.memberProperties as List<KMutableProperty<*>>
        var fields = members.map { it -> it.name }.toList();
        var types = members.map { it -> it.returnType.toString() }.toList()
        return Triple<List<String>, List<String>, List<KMutableProperty<*>>>(fields, types, members)
    }

    fun <T : Any> getResults(rs: ResultSet, entityClass: KClass<T>): List<T> {
        var retVal = mutableListOf<T>()
        var entity = entityClass.createInstance()
        var members = entity.javaClass.kotlin.memberProperties as List<KMutableProperty<*>>
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
                        "kotlin.collections.MutableList<kotlin.Int>?" ->
                            (rs.getArray(fields[i]).array as Array<out Any>).toList() as List<T>
                        "java.sql.Timestamp?" -> rs.getTimestamp(fields[i])
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

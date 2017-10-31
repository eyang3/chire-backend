package main.repositories

import org.postgresql.ds.PGPooledConnection
import java.sql.Connection
import java.sql.DriverManager
import java.sql.ResultSet
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
                println(types[i])
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
                } catch(e: Exception) {
                    println(e)
                }
                members[i].setter.call(temp, fieldValue)
            }
            retVal.add(temp)
        }
        return retVal
    }
}

package routes

import spark.Spark.*
import com.google.gson.Gson
data class Stuff(val name: String, val age: Int )
fun userRoutes() {
    val gson = Gson()
    get("/user", {req, res ->
        val stuff = Stuff("Eric", 12)
        println(stuff)
        stuff
    }, {gson.toJson(it)})
}

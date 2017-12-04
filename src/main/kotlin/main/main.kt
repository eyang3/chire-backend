package main


import com.google.gson.Gson
import spark.Spark.*
import spark.Filter
import main.repositories.DB
import main.repositories.User
import main.repositories.UserRepository
import routes.*
import kotlin.reflect.full.memberProperties




fun main(args : Array<String>) {
    port(3000)
    var p = User()
    println(p.javaClass.kotlin.memberProperties)
    staticFiles.location("/public")
    var  m = DB
    println(System.getenv("PG_PASS"))
    m.connect("jdbc:postgresql://127.0.0.1/chire", "postgres",  System.getenv("PG_PASS"))

    //allow routes to match with trailing slash
    before("/ar/*", {req, res ->
        println("Secure Route");
    })

    //set response type to json for api routes
    after(Filter({req, res ->
        if(req.pathInfo().startsWith("/api")){
            res.type("application/json")
        }
    }))

    //gzip everything
    after(Filter({req, res ->
        res.header("Content-Encoding", "gzip")
    }))

    //used to parse and convert JSON

    //used to parse and convert JSON
    val gson = Gson()


    userRoutes()
    JobRoutes()



}
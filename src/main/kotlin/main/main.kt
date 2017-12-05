package main


import com.google.gson.Gson
import spark.Spark.*
import spark.Filter
import main.repositories.DB
import main.repositories.User
import main.repositories.UserRepository
import routes.*
import kotlin.reflect.full.memberProperties


fun main(args: Array<String>) {
    port(3000)
    staticFiles.location("/public")
    DB.connect("jdbc:postgresql://127.0.0.1/chire", "postgres", System.getenv("PG_PASS"))
    userRoutes()
    JobRoutes()
    //allow routes to match with trailing slash
    before("/ar/*", { req, res ->
        val jwt = req.cookie("auth")
        if(readJWT(jwt) == null) {
            halt(403, "Unauthorized")
        }
    })
    //set response type to json for api routes
    after(Filter({ req, res ->
        res.type("application/json")
    }))
    //gzip everything
    after(Filter({ req, res ->
        res.header("Content-Encoding", "gzip")
    }))

}
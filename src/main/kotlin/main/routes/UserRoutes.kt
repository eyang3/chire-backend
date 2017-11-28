package routes

import spark.Spark.*
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import io.jsonwebtoken.Claims
import main.repositories.UserRepository
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import main.repositories.User

data class loginRequest (@SerializedName("username") val username: String,
                         @SerializedName("password") val password: String)
val gson = Gson()

fun generateJWT(user: User): String {

    var roleClaims = mutableMapOf<String, Boolean>()
    println(user.roles);
    user.roles!!.forEach { i ->
        roleClaims[i.toString()] = true;
    }
    println(roleClaims);
    var token: String = Jwts.builder()
                .setSubject(user.email)
                .setClaims(roleClaims.toMap())
                .signWith(SignatureAlgorithm.HS512, "HelloWorld")
                .compact()
    return(token)
}

fun userRoutes() {
    val userrepo = UserRepository
    post("/login", {req, res ->
        try {
            var request: loginRequest = gson.fromJson(req.body(), loginRequest::class.java)
            var user = userrepo.valid(request.username, request.password)
            if(user != null) {
                return@post(generateJWT(user));
            } else {
                return@post(RESTStatusMessage("error","login", "Invalid Username or Password"));
            }

        } catch(e: Exception) {
            println(e);
            return@post(RESTStatusMessage("error","login", "No Username or Password"));
        }
    }, {gson.toJson(it)})

    post("/create", {req, res ->
        try {
            var request: loginRequest = gson.fromJson(req.body(), loginRequest::class.java)
            userrepo.signup(request.username, request.password, 3);
            return@post(RESTStatusMessage("success", "create", "Successfully Created User"));

        } catch(e: Exception) {
            return@post(RESTStatusMessage("error", "create", e.message.toString() ));
        }

    }, {gson.toJson(it)})
}

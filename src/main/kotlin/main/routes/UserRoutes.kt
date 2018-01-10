package routes

import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import main.repositories.ApplicationRepository
import main.repositories.User
import main.repositories.UserRepository
import spark.Spark.get
import spark.Spark.post

data class loginRequest(@SerializedName("username") val username: String?,
                        @SerializedName("password") val password: String?,
                        @SerializedName("role") var role: Int?,
                        @SerializedName("token") var token: String?)

val gson = Gson()

fun generateJWT(user: User): String {

    var roleClaims = mutableMapOf<String, Any>()

    roleClaims["email"] = user.email as Any;
    roleClaims["roles"] = user.roles as Any;
    roleClaims["id"] = user.id as Any;
    var token: String = Jwts.builder()
            .setSubject(user.email)
            .setClaims(roleClaims.toMap())
            .signWith(SignatureAlgorithm.HS512, "HelloWorld")
            .compact()
    return (token)
}

fun readJWT(token: String?): User? {
    try {

        var output = Jwts.parser().setSigningKey("HelloWorld").parse(token)
        var claims = output.body as Map<String, Any>;
        return (User(claims["id"] as Int, claims["email"] as String, null, null, claims["roles"] as List<Int>, null));
    } catch (e: Exception) {
        println("Unable to validate auth token")
        return (null)
    }
}


fun userRoutes() {
    val userrepo = UserRepository
    post("/login", { req, res ->
        try {
            var request: loginRequest = gson.fromJson(req.body(), loginRequest::class.java)
            var user = userrepo.valid(request.username!!, request.password!!)
            if (user != null) {
                return@post (RESTStatusMessage("success", "create", generateJWT(user!!)));
            } else {
                return@post (RESTStatusMessage("error", "login", "Invalid Username or Password"));
            }

        } catch (e: Exception) {
            println(e);
            return@post (RESTStatusMessage("error", "login", "No Username or Password"));
        }
    }, { gson.toJson(it) })

    post("/create", { req, res ->
        try {
            var request: loginRequest = gson.fromJson(req.body(), loginRequest::class.java)
            if (request.role == null) {
                request.role = 3;
            }
            userrepo.signup(request.username!!, request.password, request.role!!);
            var user = userrepo.valid(request.username!!, request.password!!)
            return@post (RESTStatusMessage("success", "create", generateJWT(user!!)));
        } catch (e: Exception) {
            return@post (RESTStatusMessage("error", "create", e.message.toString()));
        }
    }, { gson.toJson(it) })
    post("/ar/validate", { req, res ->
        val jwt = req.cookie("auth")
        var request: loginRequest = gson.fromJson(req.body(), loginRequest::class.java)

        if (request.token == null) {
            return@post (RESTStatusMessage("success", "redirect", "do nothing"));
        }
        val redirectToken = Jwts.parser().setSigningKey("HelloWorld").parse(request.token)
        var claims = redirectToken.body as Map<String, Any>;
        var user: User = readJWT(jwt)!!
        if (user == null) {
            return@post (RESTStatusMessage("success", "redirect", "not logged in"));
        } else if (user.roles!![0] == 3 && claims["action"] as String != "apply") {
            return@post (RESTStatusMessage("success", "redirect", "invalid"));
        } else if (user.roles!![0] == 2 && claims["action"] as String == "apply") {
            return@post (RESTStatusMessage("success", "redirect", "invalid"));
        }
        println(claims);
        if (user.roles!![0] == 3) {
            ApplicationRepository.create(claims["jobRef"] as Int, user.id!!, claims["hrRef"] as Int, "", "")
            return@post (RESTStatusMessage("success", "redirect", gson.toJson(claims)));
        }
        return@post (RESTStatusMessage("success", "redirect", "do nothing"));


    }, { gson.toJson(it) })
    post("/ar/update", { req, res ->
        val jwt = req.cookie("auth")
        var user: User = readJWT(jwt)!!;
        try {
            var request: loginRequest = gson.fromJson(req.body(), loginRequest::class.java)
            userrepo.update(id = user.id!!, email = request.username!!, password = request.password!!,
                    role = user.roles!![0]);
            var user = userrepo.valid(request.username!!, request.password!!)
            return@post (RESTStatusMessage("success", "create", generateJWT(user!!)));
        } catch (e: Exception) {
            return@post (RESTStatusMessage("error", "create", e.message.toString()));
        }
    }, { gson.toJson(it) })

    get("/requestReset", { req, res ->
        try {
            val m: String = userrepo.requestReset(req.queryParams("email"));
            return@get (m);
        } catch (e: Exception) {
            return@get (RESTStatusMessage("error", "create", e.message.toString()));
        }
    }, { gson.toJson(it) })

    get("/reset/:id", { req, res ->
        var check: String = req.params("id");
        var email: String = req.queryParams("email");
        if (userrepo.isValidationString(email, check)) {
            return@get (RESTStatusMessage("ok", "reset", "Valid Reset String"));
        }
        return@get (RESTStatusMessage("error", "reset", "" +
                "Not valid reset string or expired reset string"));

    }, { gson.toJson(it) })

    get("/ar/functionsByRole", { req, res ->
        val jwt = req.cookie("auth")
        try {
            var filter: Int? = req.queryParams("filter")?.toInt()
            println("error here");
            var user: User = readJWT(jwt)!!;
            println(user);
            var menu = mutableListOf<String>()
            for (role in user.roles!!) {
                if (role == 1 && (filter == null || filter == 1)) {
                    menu.add("create")
                    menu.add("viewPostedJobs")
                    menu.add("manageContacts")
                    menu.add("organization")
                }
                if (role == 2 && (filter == null || filter == 2)) {
                    menu.add("joblistEval")
                    menu.add("toevaluate")
                }
                if (role == 3 && (filter == null || filter == 3)) {
                    menu.add("apply")
                    menu.add("joblist")
                    menu.add("searchjobs")
                }
            }
            menu.add("settings")
            return@get (menu)
        } catch (e: Exception) {
            println(e);
            return@get (RESTStatusMessage("error", "getFunctions", "" +
                    "Invalid JWT Token"));
        }
    }, { gson.toJson(it) })

    post("/reset/:id", { req, res ->
        var check: String = req.params("id");
        var email: String = req.queryParams("email");
        var request: loginRequest = gson.fromJson(req.body(), loginRequest::class.java)
        try {
            userrepo.resetPassword(email, check, request.password!!)
            return@post (RESTStatusMessage("ok", "reset", "Valid Reset String"))
        } catch (e: Exception) {
            return@post (RESTStatusMessage("error", "reset", "Unable to reset password"))
        }

    }, { gson.toJson(it) })

}

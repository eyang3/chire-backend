package routes

import com.fasterxml.jackson.databind.introspect.TypeResolutionContext
import com.google.gson.Gson
import org.junit.After
import org.junit.Before
import org.junit.Test
import main.repositories.DB
import spark.Spark
import org.apache.http.impl.client.HttpClientBuilder
import org.apache.http.client.methods.*
import org.apache.http.entity.StringEntity
import org.apache.http.impl.client.BasicCookieStore
import org.apache.http.impl.client.BasicResponseHandler
import org.apache.http.impl.cookie.BasicClientCookie


fun MakeRequest(type: String, endpoint: String, payload: String?, auth: String?): String {

    var request = when (type) {
        "POST" -> {
            val postRequest = HttpPost("http://localhost:3000/" + endpoint)
            val entity = StringEntity(payload)
            postRequest.entity = entity
            postRequest as HttpUriRequest
        }
        "GET" -> {
            val gRequest = HttpGet("http://localhost:3000/" + endpoint)
            gRequest as HttpUriRequest
        }
        "PUT" -> {
            val putRequest = HttpPut("http://localhost:3000/" + endpoint)
            val entity = StringEntity(payload)
            putRequest.entity = entity
            putRequest as HttpUriRequest
        }
        "DELETE" -> {
            val deleteRequest = HttpDelete("http://localhost:3000/" + endpoint)
            deleteRequest as HttpUriRequest
        }
        else -> {
            null
        }
    }
    var cookieStore = BasicCookieStore()


    var httpClient = HttpClientBuilder.create().setDefaultCookieStore(cookieStore).build()
    if(auth != null) {
        var cookie = BasicClientCookie("auth", auth);
        cookie.setPath("/");
        cookie.setDomain("localhost");
        cookieStore.addCookie(cookie)
    }

    request!!.addHeader("content-type", "application/json")
    val response = httpClient.execute(request)
    val handler = BasicResponseHandler()
    return (handler.handleResponse(response))
}


class JobRoutesKtTest {

    @Before
    fun setUp() {


    }

    @After
    fun tearDown() {
        DB.connect("jdbc:postgresql://127.0.0.1/chire", "postgres", System.getenv("PG_PASS"))
        DB.connection().prepareStatement("truncate table evaluations cascade").execute();
        DB.connection().prepareStatement("truncate table jobs cascade").execute();
        DB.connection().prepareStatement("truncate table users cascade").execute();
        Spark.stop()
    }

    @Test
    fun badToken() {
        var badToken = false;
        val httpClient = HttpClientBuilder.create().build()
        val gson = Gson()
        val loginInfo = loginRequest("IamSpazzy@gmail.com", "testpass", 1);
        val payload = gson.toJson(loginInfo)
        MakeRequest("POST", "create", payload, null)
        val authtoken = MakeRequest("POST", "login", payload, null)
        try {
            val body = MakeRequest("GET", "/ar/job/1", null, authtoken.substring(10))
        } catch(e: Exception) {
            badToken = true
        }
        assert(badToken)


    }

    @Test
    fun authed() {
        var badToken = true;
        val httpClient = HttpClientBuilder.create().build()
        val gson = Gson()
        val loginInfo = loginRequest("IamSpazzy@gmail.com", "testpass", role = 1);
        val payload = gson.toJson(loginInfo)
        MakeRequest("POST", "create", payload, null)
        val authtoken = MakeRequest("POST", "login", payload, null)
        try {
            val body = MakeRequest("GET", "/ar/job/1", null, authtoken)
        } catch(e: Exception) {
            badToken = false
        }
        assert(badToken)



    }


}
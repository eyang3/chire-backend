package routes

import com.google.gson.Gson
import org.junit.After
import org.junit.Before
import org.junit.Test
import main.repositories.DB


import spark.Spark
import org.apache.http.impl.client.HttpClientBuilder
import org.apache.http.HttpResponse
import org.apache.http.client.methods.HttpGet
import org.apache.http.entity.StringEntity
import org.apache.http.client.methods.HttpPost
import org.apache.http.client.methods.HttpUriRequest
import routes.*
import org.apache.http.impl.client.BasicResponseHandler


fun MakeRequest(type: String, endpoint: String, payload: String?, auth: String): String {
    val httpClient = HttpClientBuilder.create().build()
    var request = when(type) {
        "POST" -> {
            val h = HttpPost
        }
    }

    /*var request = HttpPost("http://localhost:3000/create")
    var request2 = HttpGet("http://localhost:3000/create")
    HttpDe
    request = request2;

    request.addHeader("content-type", "application/json")
    val gson = Gson()
    val entity = StringEntity(payload)
    request.entity = entity
    httpClient.exe
    val response = httpClient.execute(request)
    val handler = BasicResponseHandler()
    return(handler.handleResponse(response));*/

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
    fun jobRoutes() {
        val httpClient = HttpClientBuilder.create().build()
        val request = HttpPost("http://localhost:3000/create")

        request.addHeader("content-type", "application/json")

        val gson = Gson()
        val loginInfo = loginRequest("IamSpazzy@gmail.com", "testpass");
        val payload = gson.toJson(loginInfo)
        val entity = StringEntity(payload)
        request.entity = entity
        val response = httpClient.execute(request)
        val handler = BasicResponseHandler()
        val body = handler.handleResponse(response);
        println(body);
        


    }
    @Test
    fun authed() {

    }


}
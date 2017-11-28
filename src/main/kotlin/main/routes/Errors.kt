package routes

import com.google.gson.annotations.SerializedName


data class RESTStatusMessage (@SerializedName("status") val status: String,
                            @SerializedName("component") val component: String,
                             @SerializedName("message") val message: String)
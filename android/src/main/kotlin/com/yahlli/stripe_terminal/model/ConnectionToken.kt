package com.yahlli.stripe_terminal.model


import com.google.gson.annotations.SerializedName

data class ConnectionToken(
    @SerializedName("Data")
    val `data`: String? = "",
    @SerializedName("Exception")
    val exception: Any? = Any(),
    @SerializedName("Message")
    val message: String? = "",
    @SerializedName("State")
    val state: String? = ""
)
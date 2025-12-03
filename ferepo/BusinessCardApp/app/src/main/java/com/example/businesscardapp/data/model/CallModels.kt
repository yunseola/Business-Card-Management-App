// app/src/main/java/com/example/businesscardapp/data/model/CardCallInfoResponse.kt
package com.example.businesscardapp.data.model

import com.google.gson.annotations.SerializedName

data class CardCallInfoResponse(
    val company: String,
    val position: String?,
    val name: String,
    @SerializedName("summary") val memoSummary: String?,
    @SerializedName("imageUrlHorizontal") val imageUrlHorizontal: String?,
    @SerializedName("image1Url") val image1Url: String?
) {
    val cardImageForDisplay: String?
        get() = imageUrlHorizontal ?: image1Url
}

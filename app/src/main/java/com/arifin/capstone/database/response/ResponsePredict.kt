package com.arifin.capstone.database.response

import com.google.gson.annotations.SerializedName

data class ResponsePredict(

	@field:SerializedName("data")
	val data: Data? = null,

	@field:SerializedName("status")
	val status: Status? = null,

	@field:SerializedName("message")
	val message: String? = null
)

data class Data(

	@field:SerializedName("potential_allergen")
	val potentialAllergen: String? = null,

	@field:SerializedName("image_path")
	val imagePath: String? = null,

	@field:SerializedName("ingredients")
	val ingredients: String? = null,

	@field:SerializedName("range")
	val range: Int? = null,

	@field:SerializedName("descriptions")
	val descriptions: String? = null,

	@field:SerializedName("food")
	val food: String? = null
)

data class Status(

	@field:SerializedName("status_code")
	val statusCode: Int? = null,

	@field:SerializedName("message")
	val message: String? = null
)

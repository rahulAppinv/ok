package com.app.okra.models

import android.os.Parcel
import android.os.Parcelable

class FoodRecognintionResponse (
    var is_food: Boolean,
    var _timing: Timing?= null,
    var lang: String?=null,
    var imagecache_id: String?=null,
    var results: ArrayList<Results>?=null
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readByte() != 0.toByte(),
        parcel.readParcelable(Timing::class.java.classLoader),
        parcel.readString(),
        parcel.readString(),
        parcel.createTypedArrayList(Results)
    ) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeByte(if (is_food) 1 else 0)
        parcel.writeParcelable(_timing, flags)
        parcel.writeString(lang)
        parcel.writeString(imagecache_id)
        parcel.writeTypedList(results)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<FoodRecognintionResponse> {
        override fun createFromParcel(parcel: Parcel): FoodRecognintionResponse {
            return FoodRecognintionResponse(parcel)
        }

        override fun newArray(size: Int): Array<FoodRecognintionResponse?> {
            return arrayOfNulls(size)
        }
    }
}

class Timing (
    var foodai_totaltime: String? = null,
    var foodai_classificationtime: String?= null,
    var proxy_foodairequesttime: String?=null,
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString(),
        parcel.readString(),
        parcel.readString()
    ) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(foodai_totaltime)
        parcel.writeString(foodai_classificationtime)
        parcel.writeString(proxy_foodairequesttime)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Timing> {
        override fun createFromParcel(parcel: Parcel): Timing {
            return Timing(parcel)
        }

        override fun newArray(size: Int): Array<Timing?> {
            return arrayOfNulls(size)
        }
    }
}

class Results (
    var items: ArrayList<Items>?=null,
    var group: String?= null,
    var isSelected: Boolean= false,
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.createTypedArrayList(Items),
        parcel.readString(),
        parcel.readByte() != 0.toByte(),
    ) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeTypedList(items)
        parcel.writeString(group)
        parcel.writeByte(if (isSelected) 1 else 0)

    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Results> {
        override fun createFromParcel(parcel: Parcel): Results {
            return Results(parcel)
        }

        override fun newArray(size: Int): Array<Results?> {
            return arrayOfNulls(size)
        }
    }
}

class Items (
    var servingSizes: ArrayList<ServingSize>?=null,
    var score: Int?= null,
    var nutrition: Nutrition?= null,
    var name: String?= null,
    var food_id: String?= null,
    var group: String?= null,
    var selectedServingSize: ServingSize?= null,
    var noOfServing: String?=null,

    ) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.createTypedArrayList(ServingSize),
        parcel.readValue(Int::class.java.classLoader) as? Int,
        parcel.readParcelable(Nutrition::class.java.classLoader),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readParcelable(ServingSize::class.java.classLoader),
        parcel.readString(),
        ) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeTypedList(servingSizes)
        parcel.writeValue(score)
        parcel.writeParcelable(nutrition, flags)
        parcel.writeString(name)
        parcel.writeString(food_id)
        parcel.writeString(group)
        parcel.writeParcelable(selectedServingSize, flags)
        parcel.writeString(noOfServing)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Items> {
        override fun createFromParcel(parcel: Parcel): Items {
            return Items(parcel)
        }

        override fun newArray(size: Int): Array<Items?> {
            return arrayOfNulls(size)
        }
    }
}

class ServingSize (
    var unit: String?= null,
    var servingWeight: Double?= null,
    var isServingSelected :Boolean = false
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString(),
        parcel.readValue(Double::class.java.classLoader) as? Double,
        parcel.readByte() != 0.toByte(),
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(unit)
        parcel.writeValue(servingWeight)
        parcel.writeByte(if (isServingSelected) 1 else 0)

    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<ServingSize> {
        override fun createFromParcel(parcel: Parcel): ServingSize {
            return ServingSize(parcel)
        }

        override fun newArray(size: Int): Array<ServingSize?> {
            return arrayOfNulls(size)
        }
    }
}

class Nutrition (
    var totalCarbs: Double?= null,
    var totalFat: Double?= null,
    var protein: Double?= null,
    var calories: Double?= null
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readValue(Double::class.java.classLoader) as? Double,
        parcel.readValue(Double::class.java.classLoader) as? Double,
        parcel.readValue(Double::class.java.classLoader) as? Double,
        parcel.readValue(Double::class.java.classLoader) as? Double
    ) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeValue(totalCarbs)
        parcel.writeValue(totalFat)
        parcel.writeValue(protein)
        parcel.writeValue(calories)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Nutrition> {
        override fun createFromParcel(parcel: Parcel): Nutrition {
            return Nutrition(parcel)
        }

        override fun newArray(size: Int): Array<Nutrition?> {
            return arrayOfNulls(size)
        }
    }
}
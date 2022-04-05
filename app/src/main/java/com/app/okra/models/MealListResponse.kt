package com.app.okra.models

import android.os.Parcel
import android.os.Parcelable

class AddMealRequest(
    var date: String?=null,
    var image: String?=null,
    var foodType: String?=null,
    var calories: CommonData?=null,
    var carbs: CommonData?=null,
    var fat: CommonData?=null,
    var protien: CommonData?=null,
    var noOfServings: String?=null,
    var foodItems: ArrayList<FoodItemsRequest>?=null,
)

class MealUpdateRequest(
    var mealsId: String?= null,
    var date: String?=null,
    var image: String?=null,
    var foodType: String?=null,
    var noOfServings: String?=null,
    var calories: CommonData?=null,
    var carbs: CommonData?=null,
    var fat: CommonData?=null,
    var protien: CommonData?=null,
    var foodItems: ArrayList<FoodItemsRequest>?=null,
)

class FoodItemsRequest(
    var item: String?,
    var type: String?,
    var servingSize: String?,
)

class MealListResponse(
        var data: ArrayList<MealData>?= null
)

class MealData(
        var _id: String?= null,
        var noOfServings: String?= null,
        var userId: String?= null,
        var created: Long?=null,
        var createdAt: String?=null,
        var updatedAt: String?=null,
        var isDeleted: Boolean?=null,
        var date: String?=null,
        var image: String?=null,
        var foodType: String?=null,
        var calories: CommonData?=null,
        var carbs: CommonData?=null,
        var fat: CommonData?=null,
        var protien: CommonData?=null,
        var foodItems: List<FoodItems>?=null,
        ) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readValue(Long::class.java.classLoader) as? Long,
        parcel.readString(),
        parcel.readString(),
        parcel.readValue(Boolean::class.java.classLoader) as? Boolean,
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readParcelable(CommonData::class.java.classLoader),
        parcel.readParcelable(CommonData::class.java.classLoader),
        parcel.readParcelable(CommonData::class.java.classLoader),
        parcel.readParcelable(CommonData::class.java.classLoader),
        parcel.createTypedArrayList(FoodItems)
    ) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(_id)
        parcel.writeString(noOfServings)
        parcel.writeString(userId)
        parcel.writeValue(created)
        parcel.writeString(createdAt)
        parcel.writeString(updatedAt)
        parcel.writeValue(isDeleted)
        parcel.writeString(date)
        parcel.writeString(image)
        parcel.writeString(foodType)
        parcel.writeParcelable(calories, flags)
        parcel.writeParcelable(carbs, flags)
        parcel.writeParcelable(fat, flags)
        parcel.writeParcelable(protien, flags)
        parcel.writeTypedList(foodItems)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<MealData> {
        override fun createFromParcel(parcel: Parcel): MealData {
            return MealData(parcel)
        }

        override fun newArray(size: Int): Array<MealData?> {
            return arrayOfNulls(size)
        }
    }
}

class CommonData(
    var value :String?=null,
    var unit: String?
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString(),
        parcel.readString()
    ) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(value)
        parcel.writeString(unit)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<CommonData> {
        override fun createFromParcel(parcel: Parcel): CommonData {
            return CommonData(parcel)
        }

        override fun newArray(size: Int): Array<CommonData?> {
            return arrayOfNulls(size)
        }
    }
}

class FoodItems(
        val _id :String?=null,
         val item: String?,
         val type: String?,
         val servingSize: String?,
         val createdAt: String?,
         val updatedAt: String?,
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString()
    ) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(_id)
        parcel.writeString(item)
        parcel.writeString(type)
        parcel.writeString(servingSize)
        parcel.writeString(createdAt)
        parcel.writeString(updatedAt)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<FoodItems> {
        override fun createFromParcel(parcel: Parcel): FoodItems {
            return FoodItems(parcel)
        }

        override fun newArray(size: Int): Array<FoodItems?> {
            return arrayOfNulls(size)
        }
    }
}



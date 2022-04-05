package com.app.okra.ui.add_meal

import android.os.Parcel
import android.os.Parcelable
import com.app.okra.models.FoodRecognintionResponse

data class MealInput(
   var invalid: Boolean,
   var image: String,
   var data: FoodRecognintionResponse?=null
) : Parcelable {
   constructor(parcel: Parcel) : this(
      parcel.readByte() != 0.toByte(),
      parcel.readString()!!,
      parcel.readParcelable(FoodRecognintionResponse::class.java.classLoader)
   ) {
   }

   override fun writeToParcel(parcel: Parcel, flags: Int) {
      parcel.writeByte(if (invalid) 1 else 0)
      parcel.writeString(image)
      parcel.writeParcelable(data, flags)
   }

   override fun describeContents(): Int {
      return 0
   }

   companion object CREATOR : Parcelable.Creator<MealInput> {
      override fun createFromParcel(parcel: Parcel): MealInput {
         return MealInput(parcel)
      }

      override fun newArray(size: Int): Array<MealInput?> {
         return arrayOfNulls(size)
      }
   }
}
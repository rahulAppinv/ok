package com.app.okra.models

import android.os.Parcel
import android.os.Parcelable

data class TutorialModel(val headerText: String, val subText: String, val image :Int): Parcelable{
    var isSelected :Boolean=false

    constructor(parcel: Parcel) : this(
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readInt()
    ) {
        isSelected = parcel.readByte() != 0.toByte()
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(headerText)
        parcel.writeString(subText)
        parcel.writeInt(image)
        parcel.writeByte(if (isSelected) 1 else 0)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<TutorialModel> {
        override fun createFromParcel(parcel: Parcel): TutorialModel {
            return TutorialModel(parcel)
        }

        override fun newArray(size: Int): Array<TutorialModel?> {
            return arrayOfNulls(size)
        }
    }
}
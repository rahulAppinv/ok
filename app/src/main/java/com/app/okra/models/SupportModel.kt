package com.app.okra.models

import android.os.Parcel
import android.os.Parcelable

class SupportResponse() :Parcelable{
    var _id: String?=null
    var userId: String?=null
    var title: String?=null
    var description: String?=null
    var createdAt: String?=null
    var updatedAt: String?=null

    constructor(parcel: Parcel) : this() {
        _id = parcel.readString()
        userId = parcel.readString()
        title = parcel.readString()
        description = parcel.readString()
        createdAt = parcel.readString()
        updatedAt = parcel.readString()
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(_id)
        parcel.writeString(userId)
        parcel.writeString(title)
        parcel.writeString(description)
        parcel.writeString(createdAt)
        parcel.writeString(updatedAt)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<SupportResponse> {
        override fun createFromParcel(parcel: Parcel): SupportResponse {
            return SupportResponse(parcel)
        }

        override fun newArray(size: Int): Array<SupportResponse?> {
            return arrayOfNulls(size)
        }
    }
}
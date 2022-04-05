package com.app.okra.models

import android.os.Parcel
import android.os.Parcelable

class TestAddRequest(
        var testData: ArrayList<BLETestData>?= null
)

class TestUpdateRequest(
        var testId: String?= null,
        var bloodGlucose: Int?=null,
        var bloodPressure: Int?=null,
        var insulin: Int?=null,
        var additionalNotes: String?=null,
        var date: String?=null,
        var testingTime: String?=null,
        var deviceName: String?=null,
        var mealsBefore: ArrayList<String>?=null,
        var mealsAfter: ArrayList<String>?=null,
)


class TestListResponse(
        var data: ArrayList<Data>?= null
)


class Data(
        var _id: String?= null,
        var date: String?=null,
        var bloodGlucose: String?=null,
        var datbloodPressuree: String?=null,
        var deviceId: String?=null,
        var deviceName: String?=null,
        var insulin: String?=null,
        var testingTime: String?=null,
        var createdAt: String?=null,
        var additionalNotes: String?=null,
        ) : Parcelable {
        constructor(parcel: Parcel) : this(
                parcel.readString(),
                parcel.readString(),
                parcel.readString(),
                parcel.readString(),
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
                parcel.writeString(date)
                parcel.writeString(bloodGlucose)
                parcel.writeString(datbloodPressuree)
                parcel.writeString(deviceId)
                parcel.writeString(deviceName)
                parcel.writeString(insulin)
                parcel.writeString(testingTime)
                parcel.writeString(createdAt)
                parcel.writeString(additionalNotes)
        }

        override fun describeContents(): Int {
                return 0
        }

        companion object CREATOR : Parcelable.Creator<Data> {
                override fun createFromParcel(parcel: Parcel): Data {
                        return Data(parcel)
                }

                override fun newArray(size: Int): Array<Data?> {
                        return arrayOfNulls(size)
                }
        }
}



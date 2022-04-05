package com.app.okra.models

import android.os.Parcel
import android.os.Parcelable

class InitialBoardingResponse(
        var accessToken: String?= null,
        var userId: String?=null,
        var email: String?=null,
        var userType: String?=null,
        var name: String?=null,
        var age: String?=null,
        var mobileNo: String?=null,
        var profilePicture: String?=null,
        var pushNotificationStatus: Boolean?=null,
        var isApproved: Boolean?=null,
        var isVerify: Boolean?=null,
        var bloodGlucoseUnit: String?=null,
        ): Parcelable{
        constructor(parcel: Parcel) : this(
                parcel.readString(),
                parcel.readString(),
                parcel.readString(),
                parcel.readString(),
                parcel.readString(),
                parcel.readString(),
                parcel.readString(),
                parcel.readString(),
                parcel.readValue(Boolean::class.java.classLoader) as? Boolean,
                parcel.readValue(Boolean::class.java.classLoader) as? Boolean,
                parcel.readValue(Boolean::class.java.classLoader) as? Boolean,
                parcel.readString()
        )

        override fun writeToParcel(parcel: Parcel, flags: Int) {
                parcel.writeString(accessToken)
                parcel.writeString(userId)
                parcel.writeString(email)
                parcel.writeString(userType)
                parcel.writeString(name)
                parcel.writeString(age)
                parcel.writeString(mobileNo)
                parcel.writeString(profilePicture)
                parcel.writeValue(pushNotificationStatus)
                parcel.writeValue(isApproved)
                parcel.writeValue(isVerify)
                parcel.writeString(bloodGlucoseUnit)
        }

        override fun describeContents(): Int {
                return 0
        }

        companion object CREATOR : Parcelable.Creator<InitialBoardingResponse> {
                override fun createFromParcel(parcel: Parcel): InitialBoardingResponse {
                        return InitialBoardingResponse(parcel)
                }

                override fun newArray(size: Int): Array<InitialBoardingResponse?> {
                        return arrayOfNulls(size)
                }
        }

}



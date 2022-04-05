package com.app.okra.models

import android.os.Parcel
import android.os.Parcelable
import android.widget.ArrayAdapter

class AddMedicationRequest(
    var medicineName: String?=null,
    var unit: String?=null,
    var quantity: Int?=null,
    var medicineType: String?=null
)

class MedicationUpdateRequest(

    var medicationId: String?= null,
    var medicineName: String?=null,
    var unit: String?=null,
    var quantity: Int?=null,
    var image: ArrayList<String>?=null,
    var tags: String?=null,
    var feelings: String?=null,
)

class MedicationResponse(
    var data: ArrayList<MedicationData>?= null
)

class MedicationData (
        var _id: String?= null,
        var userId: String?= null,
        var created: Long?=null,
        var createdAt: String?=null,
        var updatedAt: String?=null,
        var image: ArrayList<String>?=null,
        var isDeleted: Boolean?=null,
        var tags: String?=null,
        var feelings: String?=null,
        var medicineName: String?=null,
        var unit: String?=null,
        var quantity: Int?=null,
        ): Parcelable{
    constructor(parcel: Parcel) : this(
        parcel.readString(),
        parcel.readString(),
        parcel.readValue(Long::class.java.classLoader) as? Long,
        parcel.readString(),
        parcel.readString(),
        parcel.createStringArrayList(),
        parcel.readValue(Boolean::class.java.classLoader) as? Boolean,
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readValue(Int::class.java.classLoader) as? Int
    ) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(_id)
        parcel.writeString(userId)
        parcel.writeValue(created)
        parcel.writeString(createdAt)
        parcel.writeString(updatedAt)
        parcel.writeStringList(image)
        parcel.writeValue(isDeleted)
        parcel.writeString(tags)
        parcel.writeString(feelings)
        parcel.writeString(medicineName)
        parcel.writeString(unit)
        parcel.writeValue(quantity)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<MedicationData> {
        override fun createFromParcel(parcel: Parcel): MedicationData {
            return MedicationData(parcel)
        }

        override fun newArray(size: Int): Array<MedicationData?> {
            return arrayOfNulls(size)
        }
    }

}

class MedicationSearchResponse(
        var data: ArrayList<MedicineName>?= null
)

class MedicineName(
        var _id: String?= null,
        var medicineName: String?= null,
        var medicineNameLower: String?=null,
        var brandName: String?=null
)

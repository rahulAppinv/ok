package com.app.okra.utils

import android.content.DialogInterface


class Listeners {

    interface BLEDataListener {
        fun onDataReceived(data: ByteArray?)
    }

    interface CommonListener {
        fun onSuccess(o: Any?, o1: Any?)
        fun onFailure(o: Any?)
    }


    interface EventClickListener{
        fun onEventClick(o: Any?, o1: Any?)
    }


    interface DialogListener {
        fun onOkClick(dialog: DialogInterface?)
        fun onCancelClick(dialog: DialogInterface?)
    }

    interface CustomMediaDialogListener {
        fun onImageClick(dialog: DialogInterface?)
        fun onUploadFromGallery(dialog: DialogInterface?)
        fun onCancelOrUploadFromEmail(dialog: DialogInterface?)
    }

    interface CustomDialogListener {
        fun onFirstOptionClick(dialog: DialogInterface?)
        fun onSecondOptionClick(dialog: DialogInterface?)
        fun onThirdOptionClick(dialog: DialogInterface?)
    }


    interface ItemClickListener {
        fun onSelect(o: Any?, o1: Any?)
        fun onUnSelect(o: Any?, o1: Any?)
    }

    interface InAdapterItemClickListener {
        fun onItemClick(o: Any?, position: Int)
    }

    interface InInterestAdapterItemClickListener {
        fun onNewItemClick(o: Any?,internalposition: Int, position: Int)
    }

    interface BottomSheetItemClickListener {
        fun onBottomSheetItemClick(o: Any?, internalPosition: Int, position: Int)
    }


    interface DisabilityDetailsItemClickListener {
        fun onDisabilityNameItemClick(o: Any?, position: Int)
        fun onCrossItemClick(o: Any?, position: Int,name :String)
    }

    interface EmergencyContactItemClickListener {
        fun onAddressClick(o: Any?, position: Int)
        fun onPostalAddressClick(o: Any?, position: Int)
        fun onRemoveEmergencyClick(o: Any?, position: Int)

    }

    interface RefereeItemClickListner {
        fun onCrossItemClick(o: Any?, position: Int)
    }

    interface LocationListener {
        fun locationSuccess(o: Any?, o1: Any?)
        fun locationFailure(o: Any?, o1: Any?)

    }

}
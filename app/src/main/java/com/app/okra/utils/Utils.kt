package com.app.okra.utils

import android.app.Activity
import android.app.DatePickerDialog
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.LocationManager
import android.net.Uri
import android.os.Build
import android.text.InputFilter
import android.util.Base64
import android.util.Log
import android.view.*
import android.widget.Button
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import com.app.okra.BuildConfig
import com.app.okra.R
import com.app.okra.data.preference.PreferenceManager
import com.app.okra.extension.beGone
import com.app.okra.extension.beVisible
import com.app.okra.models.ItemModel
import com.app.okra.ui.boarding.login.LoginActivity
import com.app.okra.utils.AppConstants.Companion.AFTER_MEAL
import com.app.okra.utils.AppConstants.Companion.AFTER_MEAL_TEXT
import com.app.okra.utils.AppConstants.Companion.ALL
import com.app.okra.utils.AppConstants.Companion.ALL_TEXT
import com.app.okra.utils.AppConstants.Companion.BEFORE_MEAL
import com.app.okra.utils.AppConstants.Companion.BEFORE_MEAL_TEXT
import com.app.okra.utils.AppConstants.Companion.CONTROLE_SOLUTION
import com.app.okra.utils.AppConstants.Companion.CONTROLE_SOLUTION_TEXT
import com.app.okra.utils.AppConstants.Companion.POST_MEDICINE
import com.app.okra.utils.AppConstants.Companion.POST_MEDICINE_TEXT
import com.app.okra.utils.AppConstants.Companion.POST_WORKOUT
import com.app.okra.utils.AppConstants.Companion.POST_WORKOUT_TEXT
import com.app.okra.utils.AppConstants.DateFormat.ISO_FORMAT
import com.app.okra.utils.AppConstants.DateFormat.ISO_FORMATE
import com.app.okra.utils.DateFormatter.DATE_DD_MM_YYYY
import com.google.android.gms.maps.model.LatLng
import kotlinx.android.synthetic.main.dialog_add_new.*
import java.io.File
import java.io.UnsupportedEncodingException
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import java.text.DateFormat
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit
import java.util.regex.Pattern

fun showAlertDialog(
        context: Context?,
        listener: Listeners.DialogListener?,
        msg: String?,
        needCancelButton: Boolean
) {
    val dialog: AlertDialog.Builder = AlertDialog.Builder(context!!)
    dialog.setCancelable(false)
    dialog.setMessage(msg)
    dialog.setPositiveButton("Ok") { dialog1, which ->
        if (listener != null) {
            listener.onOkClick(dialog1)
        } else {
            dialog1.dismiss()
        }
    }
    if (needCancelButton) {
        dialog.setNegativeButton("Cancel") { dialog, _ ->
            if (listener != null) {
                listener.onCancelClick(dialog)
            } else {
                dialog.dismiss()
            }
        }
    }
    dialog.show()
}

fun Context.sendEmail(email: String, subject: String) {
    val intent = Intent(Intent.ACTION_SENDTO).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_EMAIL, email)
        putExtra(Intent.EXTRA_SUBJECT, subject)
        putExtra(Intent.EXTRA_TEXT, "")
    }
    startActivity(Intent.createChooser(intent, "Send Email"))
}

fun Context.openLink(url: String) {
    val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
    startActivity(browserIntent)
}


fun showAlertDialog(
        context: Context?,
        listener: Listeners.DialogListener?,
        msg: String?,
        needCancelButton: Boolean,
        positiveButtonText: String?,
        negativeButtonText: String?,
        title: String = ""
) {
    val dialog: AlertDialog.Builder = AlertDialog.Builder(context!!)
    dialog.setCancelable(false)
    if (title.isNotEmpty()) {
        dialog.setTitle(title)
    }
    dialog.setMessage(msg)
    dialog.setPositiveButton(positiveButtonText) { dialog, _ ->
        if (listener != null) {
            listener.onOkClick(dialog)
        } else {
            dialog.dismiss()
        }
    }
    if (needCancelButton) {
        dialog.setNegativeButton(negativeButtonText) { dialog, _ ->
            if (listener != null) {
                listener.onCancelClick(dialog)
            } else {
                dialog.dismiss()
            }
        }
    }
    dialog.show()
}


fun getKeyHash(context: Context) {
    try {
        val info: PackageInfo = context.getPackageManager().getPackageInfo(
                context.packageName,
                PackageManager.GET_SIGNATURES
        )
        for (signature in info.signatures) {
            val md: MessageDigest = MessageDigest.getInstance("SHA")
            md.update(signature.toByteArray())
            val hashKey: String = String(Base64.encode(md.digest(), 0))
            Log.i("TAG", "printHashKey() Hash Key: $hashKey")
        }
    } catch (e: NoSuchAlgorithmException) {
        Log.e("TAG", "printHashKey()", e)
    } catch (e: Exception) {
        Log.e("TAG", "printHashKey()", e)
    }

}

var builder: AlertDialog.Builder? = null
var dialog: Dialog? = null

fun showProgressDialog(context: Activity, cancelable: Boolean): AlertDialog {
    val builder = AlertDialog.Builder(context)
    val inflater = context.layoutInflater
    val view = inflater.inflate(R.layout.layout_loader, null)
    builder.setView(view)
    builder.setCancelable(cancelable)
    return builder.create()
}

fun getInputFilter(): InputFilter {
    return InputFilter { source, start, end, dest, dstart, dend ->
        for (index in start until end - 1) {
            val type = Character.getType(source[index])
            if (type == Character.SURROGATE.toInt()) {
                return@InputFilter ""
            }
        }
        null
    }
}

fun getTimeStampFromDate(selectDate: String?, formatYouWant: String? = null): Long? {
    var fromFormat = "dd/MM/yyyy"

    if (formatYouWant != null) {
        fromFormat = formatYouWant
    }
    val formatter: DateFormat = SimpleDateFormat(fromFormat, Locale.getDefault())
    var date: Date? = null
    try {
        date = formatter.parse(selectDate)
    } catch (e: ParseException) {
        e.printStackTrace()
    }
    return date!!.time
}


fun getDateFromTimeStamp(timeStamp: Long?, formatYouWant: String? = null): String? {
    var fromFormat = "dd/MM/yyyy"

    if (formatYouWant != null) {
        fromFormat = formatYouWant
    }
    if (timeStamp != null) {
        val dateInstance = Date(timeStamp)

        val formatter: DateFormat = SimpleDateFormat(fromFormat, Locale.getDefault())
        try {
            return formatter.format(dateInstance)
        } catch (e: ParseException) {
            e.printStackTrace()
        }
    }
    return ""
}

fun convertMGDLtoMMOL(mgdlValue: Float): String {
    // return String.format("%.2f", (mgdlValue * 0.0555).toString())
    return (mgdlValue * 0.0555).toString()
}
fun convertMMOLtoMGDL(mmolValue: Float): String {
    // return String.format("%.2f", (mmolValue / 0.0555).toString())
    return(mmolValue / 0.0555).toString()
}



fun getDateOnly(year: Int?, month: Int?, day: Int?): String {
    val cal: Calendar = GregorianCalendar()
    cal.set(year!!, month!!, day!!)
    val selectedDate: Date = cal.time
    val targetFormat: DateFormat = SimpleDateFormat("EEEE MMM dd", Locale.ENGLISH)
    var result = ""
    try {
        result = targetFormat.format(selectedDate)
    } catch (e: ParseException) {
        e.printStackTrace()
    }
    return result
}


fun getISOFromDateAndTime(year: Int?, month: Int?, day: Int?, hour: Int, min: Int): String? {
    val cal: Calendar = GregorianCalendar()
    if (hour == -1 && min == 0) { //for 00.00 hours
        cal.set(year!!, month!!, day!!, 0, 0, 0)
    } else if (hour == 0 && min == 0) {
        cal.set(year!!, month!!, day!!, 12, 0, 0)
    } else {
        cal.set(year!!, month!!, day!!, hour!!, min!!, 0)
    }

    val selectedDate: Date = cal.time
    val formatISO = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
        SimpleDateFormat(ISO_FORMATE, Locale.getDefault())
    } else {
        SimpleDateFormat(ISO_FORMAT, Locale.getDefault())
    }
    formatISO.timeZone = TimeZone.getTimeZone("UTC")

    var result = ""
    try {

        result = formatISO.format(selectedDate)
    } catch (e: ParseException) {
        e.printStackTrace()
    }
    return result
}

fun getISOFromDate(date: String, currentFormat: String?, needUTC: Boolean = true): String {

    val formatter: DateFormat = SimpleDateFormat(currentFormat, Locale.getDefault())
    val dateInDateFormat =  formatter.parse(date)
    val formatISO = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
        SimpleDateFormat(ISO_FORMATE, Locale.getDefault())
    } else {
        SimpleDateFormat(ISO_FORMAT, Locale.getDefault())
    }
    if(needUTC)
        formatISO.timeZone = TimeZone.getTimeZone("UTC")

    var result = ""

    if(dateInDateFormat!=null) {
        try {
            result = formatISO.format(dateInDateFormat)
        } catch (e: ParseException) {
            e.printStackTrace()
        }
    }
    return result
}

fun getISOFromDateAndTime_inDate(date: Date?): Date? {
    val formatISO = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
        SimpleDateFormat(ISO_FORMATE, Locale.getDefault())
    } else {
        SimpleDateFormat(ISO_FORMAT, Locale.getDefault())
    }
    formatISO.timeZone = TimeZone.getTimeZone("UTC")
    var result: Date? = null

    if (date != null) {
        try {
            result = formatISO.parse(formatISO.format(date))
        } catch (e: ParseException) {
            e.printStackTrace()
        }
    }
    return result
}


fun getDateFromDateISO(
        selectDate: String?,
        formatYouWant: String? = null,
        needFormatYouWantInUtc: Boolean = true,
        needCurrentFormatInUtc: Boolean = false
): Date? {
    var fromFormat = "dd/MM/yyyy"

    if (formatYouWant != null) {
        fromFormat = formatYouWant
    }

    // Required Format
    val formatter: DateFormat = SimpleDateFormat(fromFormat, Locale.getDefault())
    if (needFormatYouWantInUtc)
        formatter.timeZone = TimeZone.getTimeZone("UTC")

    if (needCurrentFormatInUtc) {
        // Initial Format for converting ISO date to UTC
        val formatISO = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            SimpleDateFormat(ISO_FORMATE, Locale.getDefault())
        } else {
            SimpleDateFormat(ISO_FORMAT, Locale.getDefault())
        }
        formatISO.timeZone = TimeZone.getTimeZone("UTC")

        val dateInISO = formatISO.parse(selectDate)

        try {
            return formatter.parse(formatter.format(dateInISO))
        } catch (e: ParseException) {
            e.printStackTrace()
        }
    } else {
        try {
            return formatter.parse(selectDate)
        } catch (e: ParseException) {
            e.printStackTrace()
        }
    }
    return null
}


fun getDateTimeFromISO(iSODate: String, formatYouWant: String? = "EEE MMM dd, HH:mm"): String? {

    val formatISO = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
        SimpleDateFormat(ISO_FORMATE, Locale.getDefault())
    } else {
        SimpleDateFormat(ISO_FORMAT, Locale.getDefault())
    }
    formatISO.timeZone = TimeZone.getTimeZone("UTC")

    val targetFormat: DateFormat = SimpleDateFormat(formatYouWant, Locale.getDefault())
    val date = formatISO.parse(iSODate)
    var result = ""
    try {
        result = targetFormat.format(date)
    } catch (e: ParseException) {
        e.printStackTrace()
    }
    return result
}

fun getDateFromISOInString(
        iSODate: String,
        formatYouWant: String = "hh:mm a, EEE MMM dd",
        dateInUTC: Boolean = true
): String {
    // val formatISO = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS", Locale.getDefault());
    val formatISO = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
        SimpleDateFormat(ISO_FORMATE, Locale.getDefault())
    } else {
        SimpleDateFormat(ISO_FORMAT, Locale.getDefault())
    }
    if (dateInUTC) {
        formatISO.timeZone = TimeZone.getTimeZone("UTC")
    }
    val targetFormat: DateFormat = SimpleDateFormat(formatYouWant, Locale.ENGLISH)
    val date = formatISO.parse(iSODate)
    var result = ""
    try {
        result = targetFormat.format(date)
    } catch (e: ParseException) {
        e.printStackTrace()
    }
    return result
}

fun getDateFromISOInDate(
        selectDate: String?,
        formatYouWant: String? = null,
        needInUtc: Boolean = true
): Date? {
    var fromFormat = ISO_FORMATE
    if (formatYouWant != null)
        fromFormat = formatYouWant
    /*val formatter: DateFormat = SimpleDateFormat(fromFormat, Locale.US)

    if(needInUtc)
        formatter.timeZone= TimeZone.getTimeZone("UTC")
    var date: Date? = null
    try {
        date = formatter.parse(selectDate)
    } catch (e: ParseException) {
        e.printStackTrace()
    }
    return date!!*/

    return getDateFromDateISO(selectDate, fromFormat, needInUtc)
}


fun showCustomAlertDialog(
        context: Context?,
        listener: Listeners.DialogListener?,
        message: String? = null,
        needCancelButton: Boolean,
        positiveButtonText: String? = null,
        negativeButtonText: String? = null,
        title: String? = null
) {
    dialog = Dialog(context!!, R.style.MyCustomTheme)
    val view: View = LayoutInflater.from(context).inflate(R.layout.dialog_custom_alert, null)
    dialog?.apply {
        setContentView(view)
        setCanceledOnTouchOutside(true)

        val lp = dialog!!.window!!.attributes
        lp.height = ViewGroup.LayoutParams.WRAP_CONTENT
        lp.width = ViewGroup.LayoutParams.WRAP_CONTENT
        lp.gravity = Gravity.CENTER
        lp.dimAmount = 0.5f
        window?.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);

        lp.windowAnimations = R.style.DialogAnimation
        window?.attributes = lp


        /*if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window?.insetsController?.hide(WindowInsets.Type.statusBars())
        } else {
            window?.setFlags(
                    WindowManager.LayoutParams.FLAG_FULLSCREEN,
                    WindowManager.LayoutParams.FLAG_FULLSCREEN
            )
        }*/

        val btnPositive: Button = findViewById(R.id.btnPositive)
        val btnNegative: Button = findViewById(R.id.btnNegative)
        val tvMessage: TextView = findViewById(R.id.tvMessage)
        val tvTitle: TextView = findViewById(R.id.tvTitle)

        positiveButtonText?.let {
            btnPositive.text = it
        }

        title?.let {
            tvTitle.text = it
        }

        if (needCancelButton) {
            btnNegative.beVisible()
            negativeButtonText?.let {
                btnNegative.text = it
            }
        } else {
            btnNegative.beGone()
        }

        message?.let {
            tvMessage.text = it
        }

        btnPositive.setOnClickListener {
            listener?.onOkClick(dialog)
            dialog?.dismiss()
        }

        btnNegative.setOnClickListener {
            dialog?.dismiss()
            listener?.onCancelClick(dialog)
        }

        show()
    }
}

@RequiresApi(api = Build.VERSION_CODES.KITKAT)
public fun getFileSize(uri: Uri): Long {
    try {
        val file = File(uri.path);
        if (file.exists()) {
            var fileSize = file.length();
            if (fileSize >= 1024) {
                fileSize /= 1024; //For KB

                return if (fileSize > 1024)
                    fileSize / 1024; //For MB
                else
                    0;

            }
            return fileSize;
        }
    } catch (e: Exception) {
        Log.d("Error", e.toString());
    }
    return -1;
}


/*fun getUserData(): UserDetailResponse {
    val userData = PreferenceManager.getString(AppConstants.Pref_Key.USER_DATA)
    return Gson().fromJson(userData, UserDetailResponse::class.java)
}*/

fun getFullName(firstName: String?, lastName: String?): String? {
    return if (!firstName.isNullOrEmpty()) {
        if (!lastName.isNullOrEmpty()) {
            return "$firstName $lastName"
        }
        return firstName
    } else null
}


fun getAddressFromLatLng(latLng: LatLng, mContext: Context?): Address? {
    val geocoder = Geocoder(mContext)
    val addresses: List<Address>?
    return try {
        addresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 5)
        addresses?.get(0)
    } catch (e: java.lang.Exception) {
        e.printStackTrace()
        null
    }
}


/*fun navigateToLogin(activity: FragmentActivity) {
    val isSupporterCoachMarks =
            PreferenceManager.getBoolean(AppConstants.Pref_Key.IS_SUPPORTER_COACHMARK_SHOW)
    val isParticipantCoachMarks =
            PreferenceManager.getBoolean(AppConstants.Pref_Key.IS_PARTICIPANT_COACHMARK_SHOW)
    val deviceToken = PreferenceManager.getString(AppConstants.Pref_Key.DEVICE_TOKEN)

    // CLEAR ALL PREF DATA
    PreferenceManager.clearAllPrefs()
    PreferenceManager.putBoolean(AppConstants.Pref_Key.IS_LOGIN_PREVIOUSLY, true)
    PreferenceManager.putBoolean(
            AppConstants.Pref_Key.IS_SUPPORTER_COACHMARK_SHOW,
            isSupporterCoachMarks
    )
    PreferenceManager.putBoolean(
            AppConstants.Pref_Key.IS_PARTICIPANT_COACHMARK_SHOW,
            isParticipantCoachMarks
    )
    PreferenceManager.putString(
            AppConstants.Pref_Key.DEVICE_TOKEN,
            deviceToken
    )

    GlobalScope.launch {
        val appDatabase: AppDb? = AppDb.invoke(activity)
        appDatabase?.searchHistoryDao()?.deleteSearchHistory()
    }
    ActivityCompat.finishAffinity(activity);
    activity.startActivity(
            Intent(activity, LoginActivity::class.java)
                    .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                    .putExtra(AppConstants.Intent_Constant.FROM_SCREEN, AppConstants.LOGIN)
    )
}*/

fun getAgeFromDob(dob: Long?): String? {
    val dateNew = Date()
    dateNew.time = dob!!
    val dateofbirth = SimpleDateFormat("yyyy", Locale.US).format(dateNew)

    val cal = Calendar.getInstance()
    val age = (cal.get(Calendar.YEAR) - dateofbirth.toInt()).toString()
    return age
}

fun getDateFromMonth(month: Int, year: Int): Date? {
    val calendar = Calendar.getInstance()
    calendar[year, month] = 1
    return calendar.time
}


fun checkLocationEnabled(context: Context?): Boolean {

    context?.apply {
        val lm: LocationManager =
            context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        var gps_enabled = false
        var network_enabled = false

        try {
            gps_enabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER)
        } catch (ex: java.lang.Exception) {
        }

        try {
            network_enabled = lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
        } catch (ex: java.lang.Exception) {
        }

        if (!gps_enabled && !network_enabled) {
            return false
        }
    }

    return true
}

/*
@initialFormat : It can be empty. If user set it empty. We will consider by default font "dd-MM-yyyy".
@formatYouWant: It must not be empty. This is the form you need.
*/
fun getDifferentInfoFromDate(
        dateToConvert: String,
        initFormat: String?,
        formatYouWant: String
): Date? {
    var date: Date? = null
    var initialFormat: String? = initFormat

    if (initialFormat == null || initialFormat.isEmpty()) {
        initialFormat = "dd-MM-yyyy";
    }
    val dayFormat = SimpleDateFormat(formatYouWant, Locale.getDefault());
    try {
        date = SimpleDateFormat(initialFormat, Locale.getDefault()).parse(dateToConvert);

    } catch (e: ParseException) {
        e.printStackTrace();
    }
    return date;
}

/*
@initialFormat : It can be empty. If user set it empty. We will consider by default font "dd-MM-yyyy".
@formatYouWant: It must not be empty. This is the form you need.
*/
fun getDifferentInfoFromDateInString(
        dateToConvert: String,
        initFormat: String?,
        formatYouWant: String
): String? {
    val date: Date?
    var initialFormat: String? = initFormat

    if (initialFormat == null || initialFormat.isEmpty()) {
        initialFormat = "dd-MM-yyyy";
    }
    val dayFormat = SimpleDateFormat(formatYouWant, Locale.getDefault());
    try {
        date = SimpleDateFormat(initialFormat, Locale.getDefault()).parse(dateToConvert);
        return dayFormat.format(date!!);

    } catch (e: ParseException) {
        e.printStackTrace();
    }
    return null
}

fun compareDates(
        selectedDate: Long,
        currentDate: Long,
        formatYouWant: String? = "dd-MM-yyyy HH:mm"
): Boolean {
    var selectedDateInstance: Date? = null
    var currentDateInstance: Date? = null

    val dateFormatToCheck = SimpleDateFormat(formatYouWant, Locale.getDefault())

    val stringSelectedDate = dateFormatToCheck.format(Date(selectedDate))
    val stringCurrentDate = dateFormatToCheck.format(Date(currentDate))

    try {
        selectedDateInstance = dateFormatToCheck.parse(stringSelectedDate)
        currentDateInstance = dateFormatToCheck.parse(stringCurrentDate)

        return (selectedDateInstance == currentDateInstance)

    } catch (e1: ParseException) {
        e1.printStackTrace()
    }
    return false
}

fun compareAndGetDateToSet(
        startTime: String,
        endTime: String,
        needCurrentFormatInUtc: Boolean,
        formatYouWantToCompare: String? = null,
        needFormatYouWantInUtc: Boolean = false
): String {
    val sdate = getDateFromDateISO(
            startTime, formatYouWantToCompare,
            needCurrentFormatInUtc = needCurrentFormatInUtc,
            needFormatYouWantInUtc = needFormatYouWantInUtc
    )
    val edate = getDateFromDateISO(
            endTime, formatYouWantToCompare,
            needCurrentFormatInUtc = needCurrentFormatInUtc,
            needFormatYouWantInUtc = needFormatYouWantInUtc,
    )
    val sTime = getDateTimeFromISO(startTime)

    return when {
        edate!! == sdate -> {
            val eTime = getDateTimeFromISO(endTime, "HH:mm")
            "$sTime - $eTime"
        }
        edate.after(sdate) -> {
            val eTime = getDateTimeFromISO(endTime)
            "$sTime - $eTime"
        }
        else -> {
            sTime!!
        }
    }
}


public fun compareAndGetDateMatchStatus(
        time1: String,
        time2: String,
        needCurrentFormatInUtc: Boolean,
        formatYouWantToCompare: String? = null
): Int {
    val t1date = getDateFromDateISO(time1, formatYouWantToCompare, needCurrentFormatInUtc)
    val t2date = getDateFromDateISO(time2, formatYouWantToCompare, needCurrentFormatInUtc)
    return when {
        t2date!! == t1date -> {
            0
        }
        t2date.after(t1date) -> {
            1
        }
        else -> {
            -1
        }
    }
}


/*
* Encode the text and smiley using base64
* */
fun encodeSmiley(old: String): String? {
    val st = old.trim { it <= ' ' }.replace("\n".toRegex(), " ")
    var data = ByteArray(0)
    var base64 = st
    try {
        data = st.toByteArray(charset("UTF-8"))
        base64 = Base64.encodeToString(data, Base64.DEFAULT)
    } catch (e: UnsupportedEncodingException) {
        e.printStackTrace()
        return base64
    }
    base64 = base64.replace("\n".toRegex(), "")
    return base64
    //return old;
}

fun decodeSmiley(`in`: String?): String? {
    var `in` = `in`
    if (`in` != null && `in`.isNotEmpty()) {
        var text = `in`
        try {
            val regex = "([A-Za-z0-9+/]{4})*" +
                    "([A-Za-z0-9+/]{4}|[A-Za-z0-9+/]{3}=|[A-Za-z0-9+/]{2}==)"
            val patron = Pattern.compile(regex)
            `in` = `in`.replace("\n", "")
            text = if (!patron.matcher(`in`).matches()) {
                return text
            } else {
                val data = Base64.decode(`in`, Base64.DEFAULT)
                String(data)
            }
        } catch (e: UnsupportedEncodingException) {
            e.printStackTrace()
            return text
        }
        return text
    }
    return `in`
}


fun navigateToLogin(activity: FragmentActivity) {

    val deviceToken = PreferenceManager.getString(AppConstants.Pref_Key.DEVICE_TOKEN)
    val isFirstTime = PreferenceManager.getBoolean(AppConstants.Pref_Key.IS_FIRST_TIME)

    // CLEAR ALL PREF DATA
    PreferenceManager.clearAllPrefs()
    PreferenceManager.putBoolean(AppConstants.Pref_Key.IS_LOGGED_IN, false)
    PreferenceManager.putString(AppConstants.Pref_Key.DEVICE_TOKEN, deviceToken)
    PreferenceManager.putBoolean(AppConstants.Pref_Key.IS_FIRST_TIME, isFirstTime)

    /*GlobalScope.launch {
        val appDatabase: AppDb? = AppDb.invoke(activity)
        appDatabase?.searchHistoryDao()?.deleteSearchHistory()
    }*/
    ActivityCompat.finishAffinity(activity);
    activity.startActivity(
            Intent(activity, LoginActivity::class.java)
                    .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                    .putExtra(AppConstants.Intent_Constant.FROM_SCREEN, AppConstants.LOGIN)
    )
}


fun getProfileItems(context: Context?): MutableMap<Int, ItemModel> {
    val profileHash = mutableMapOf<Int, ItemModel>()

    profileHash[0] = ItemModel("Personal informations", R.mipmap.personal_info)
    profileHash[1] = ItemModel(
            "My subscriptions",
            R.mipmap.my_subs
    )
    profileHash[2] = ItemModel("Connected devices", R.mipmap.connected_device)
    profileHash[3] = ItemModel(
            "Settings",
            R.mipmap.setting
    )
    profileHash[4] = ItemModel("App tutorial", R.mipmap.app_tutorial)
    profileHash[5] = ItemModel("Change password", R.mipmap.change_password)
    profileHash[6] = ItemModel("Share app", R.mipmap.share_app)
    profileHash[7] = ItemModel("My reminders", R.mipmap.my_reminder)
    profileHash[8] = ItemModel("Support request", R.mipmap.support_request)


    return profileHash
}

fun getSettingsItems(context: Context?): MutableMap<Int, ItemModel> {
    val profileHash = mutableMapOf<Int, ItemModel>()

    profileHash[0] = ItemModel("About us", R.mipmap.about_us)
    profileHash[1] = ItemModel(
            "Privacy policy",
            R.mipmap.privacy_policy
    )
    profileHash[2] = ItemModel("Terms and conditions", R.mipmap.terms_conditions)

    profileHash[3] = ItemModel("Notifications settings", R.mipmap.notifications_settings)
    profileHash[4] = ItemModel("Measurement settings", R.mipmap.measurement_settings)
    profileHash[5] = ItemModel("FAQ", R.mipmap.faq)
    profileHash[6] = ItemModel("Contact us", R.mipmap.contact_us)

    return profileHash
}


fun showOptionDialog(
        context: Context?,
        listener: Listeners.CustomMediaDialogListener,
        isUploadFromEmail: Boolean
) {
    dialog = Dialog(context!!, R.style.MyCustomTheme)
    val view: View = LayoutInflater.from(context).inflate(R.layout.dialog_more_option, null)
    dialog?.apply {
        setContentView(view)
        setCanceledOnTouchOutside(true)

        val lp = dialog!!.window!!.attributes
        lp.height = ViewGroup.LayoutParams.WRAP_CONTENT
        lp.gravity = Gravity.BOTTOM
        lp.dimAmount = 0.5f
        window?.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);

        lp.windowAnimations = R.style.DialogAnimation
        window?.attributes = lp


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window?.insetsController?.hide(WindowInsets.Type.statusBars())
        } else {
            window?.setFlags(
                    WindowManager.LayoutParams.FLAG_FULLSCREEN,
                    WindowManager.LayoutParams.FLAG_FULLSCREEN
            )
        }

        val clCamera: ConstraintLayout = findViewById(R.id.clCamera)
        val clLibrary: ConstraintLayout = findViewById(R.id.clLibrary)


        clCamera.setOnClickListener {
            listener.onImageClick(dialog)
            dialog?.dismiss()
        }

        clLibrary.setOnClickListener {
            dialog?.dismiss()
            listener.onUploadFromGallery(dialog)
        }
        show()
    }
}

fun showAddNewDialog(
        context: Context?,
        listener: Listeners.CustomDialogListener
) {
    dialog = Dialog(context!!, R.style.MyCustomTheme)
    val view: View = LayoutInflater.from(context).inflate(R.layout.dialog_add_new, null)
    dialog?.apply {
        setContentView(view)
        setCanceledOnTouchOutside(true)

        val lp = dialog!!.window!!.attributes
        lp.height = ViewGroup.LayoutParams.WRAP_CONTENT
        lp.gravity = Gravity.BOTTOM
        lp.dimAmount = 0.5f
        window?.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);

        lp.windowAnimations = R.style.DialogAnimation
        window?.attributes = lp

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window?.insetsController?.hide(WindowInsets.Type.statusBars())
        } else {
            window?.setFlags(
                    WindowManager.LayoutParams.FLAG_FULLSCREEN,
                    WindowManager.LayoutParams.FLAG_FULLSCREEN
            )
        }

        val clAddTest: CardView = findViewById(R.id.clAddTest)
        val clAddMeal: CardView = findViewById(R.id.clAddMeal)


        clAddTest.setOnClickListener {
            listener.onFirstOptionClick(dialog)
            dialog?.dismiss()
        }

        clAddMeal.setOnClickListener {
            dialog?.dismiss()
            listener.onSecondOptionClick(dialog)
        }

        clAddMed.setOnClickListener {
            dialog?.dismiss()
            listener.onThirdOptionClick(dialog)
        }
        show()
    }
}

fun getMealTime(mealTime: String, forValue: Boolean = true): String {

    if (forValue) {
        return when (mealTime) {
            AFTER_MEAL -> {
                AFTER_MEAL_TEXT
            }
            BEFORE_MEAL -> {
                BEFORE_MEAL_TEXT
            }
            CONTROLE_SOLUTION -> {
                CONTROLE_SOLUTION_TEXT
            }
            POST_MEDICINE -> {
                POST_MEDICINE_TEXT
            }
            POST_WORKOUT -> {
                POST_WORKOUT_TEXT
            }
            else -> ALL_TEXT
        }
    }
    else {
        return when (mealTime) {
            AFTER_MEAL_TEXT-> {
                AFTER_MEAL
            }
            BEFORE_MEAL_TEXT -> {
                BEFORE_MEAL
            }
            CONTROLE_SOLUTION_TEXT-> {
                CONTROLE_SOLUTION
            }
            POST_MEDICINE_TEXT -> {
                POST_MEDICINE
            }
            POST_WORKOUT_TEXT-> {
                POST_WORKOUT
            }
            else -> ALL
        }
    }
}

fun convertLocalTimeZoneToUTC(inputPattern: String?, date: String?): String {
    val utcDateFormat = SimpleDateFormat(inputPattern, Locale.US)
    var localDate: Date? = null
    return try {
        localDate = SimpleDateFormat(
            inputPattern,
            Locale.getDefault()
        ).parse(date) // Local Date Format (By default)
        utcDateFormat.timeZone = TimeZone.getTimeZone("UTC")
        utcDateFormat.format(localDate)
        localDate.toString()
    } catch (e: ParseException) {
        e.printStackTrace()
        ""
    }
}

fun convertUtc2Local(utcTime: String?, date_formate: String?): String? {
    var time = ""
    if (utcTime != null) {
        val utcFormatter = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
        utcFormatter.timeZone = TimeZone.getTimeZone("UTC")
        var gpsUTCDate: Date? = null //from  ww  w.j  a va 2 s  . c  o  m
        try {
            gpsUTCDate = utcFormatter.parse(utcTime)
        } catch (e: ParseException) {
            e.printStackTrace()
        }
        val localFormatter = SimpleDateFormat(date_formate, Locale.getDefault())
        localFormatter.timeZone = TimeZone.getDefault()
        if (BuildConfig.DEBUG && gpsUTCDate == null) {
            error("Assertion failed")
        }
        time = localFormatter.format(gpsUTCDate!!.time)
        val c = Calendar.getInstance()
        val currentDate = localFormatter.format(c.time)
        if (currentDate.equals(time)) {
            time = "Today"
        }

    }
    return time
}

fun getDateFromPattern(inputPattern: String?, date: String?): Date? {
    val utcDateFormat = SimpleDateFormat(inputPattern, Locale.US)
    var localDate: Date? = null
    return try {
        localDate = SimpleDateFormat(inputPattern, Locale.US).parse(date) // Local Date Format (By default)
        utcDateFormat.timeZone = TimeZone.getTimeZone("UTC")
        utcDateFormat.format(localDate)
        localDate
    } catch (e: ParseException) {
        e.printStackTrace()
        null
    }
}


fun getCurrentDateInString(formatYouWant: String? = DATE_DD_MM_YYYY): String {

    val date = Date(System.currentTimeMillis())
    val sdf = SimpleDateFormat(formatYouWant, Locale.getDefault())

    try {
        return sdf.format(date)
    }catch (e: java.lang.Exception){
       e.printStackTrace()
    }
    return ""
}

fun getPastTimeString(createdTimestamp: Long?): String {
    var convertedTime = ""
    var convertedSecTime = ""
    val suffix = "ago"
    try {
        val nowTime = System.currentTimeMillis()

        if (createdTimestamp != null) {
            var value = 0L
            var text = ""
            val dateDiff = nowTime - createdTimestamp
            val second = TimeUnit.MILLISECONDS.toSeconds(dateDiff)
            val minute = TimeUnit.MILLISECONDS.toMinutes(dateDiff)
            val hour = TimeUnit.MILLISECONDS.toHours(dateDiff)
            val day = TimeUnit.MILLISECONDS.toDays(dateDiff)
            when {
                second < 60 -> {
                    value = second
                    text = "second"
                 //   convertedSecTime = "Just now"
                }
                minute < 2 -> {
                    value = minute
                    text = "minute"
                }
                minute < 60 -> {
                    value = minute
                    text = "minutes"
                }
                hour < 2 -> {
                    value = hour
                    text = "hour"
                }
                hour < 24 -> {
                    value = hour
                    text = "hours"
                }
                day < 2 -> {
                    value = day
                    text = "day"
                }
                day < 7 -> {
                    value = day
                    text = "days"
                }
                else -> {
                    when {
                        day < 14 -> {
                            value = day / 7
                            text = "week"
                        }
                        day < 30 -> {
                            value = day / 7
                            text = "weeks"
                        }
                        day < 60 -> {
                            value = day / 30
                            text = "month"
                        }
                        day < 360 -> {
                            value = day / 30
                            text = "months"
                        }
                        day < 720 -> {
                            value = day / 360
                            text = "year"
                        }
                        day > 720 -> {
                            value = day / 360
                            text = "years"
                        }
                    }
                }
            }

            convertedTime = if (convertedSecTime.isNotEmpty()) {
                convertedSecTime
            } else {
                "$value $text $suffix"
            }
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
    return convertedTime
}

fun getDatePicker(
        context: Context,
        dateSelected: (Long, Boolean) -> Unit,
        isEndDate: Boolean
): DatePickerDialog {
    val calendar = Calendar.getInstance()
    return DatePickerDialog(context, { _, year, month, dayOfMonth ->
        calendar.set(year, month, dayOfMonth)
        val longTime = calendar.timeInMillis
        dateSelected(longTime, isEndDate)
    },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
    )
}

fun getDatePicker(
        context: Context,
        dateSelected: (Long, Boolean) -> Unit,
        setDateInMillis: Long,
        isEndDate: Boolean,
): DatePickerDialog {
    val calendar = Calendar.getInstance()
    calendar.timeInMillis = setDateInMillis
    return DatePickerDialog(context, { _, year, month, dayOfMonth ->
        calendar.set(year, month, dayOfMonth)
        val longTime = calendar.timeInMillis
        dateSelected(longTime, isEndDate)
    },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
    )
}

fun getMinDateForReports(): Long {
    val calendar = Calendar.getInstance()
    calendar.set(
            calendar.get(Calendar.YEAR) - 1,
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
    )
    return calendar.timeInMillis
}


fun getPrimaryColor(context: Context): Int{
    return ContextCompat.getColor(context, R.color.colorPrimary)
}


// "needAllUppercase" -> If TRUE, uppercase first letter of all word .
fun convertWordsToUpperOrLowerCase(string: String, needUpperCase: Boolean, needAllUppercase: Boolean): String{
    val words: List<String> = string.split(" ")
    var modifiedString = ""


    if(needAllUppercase) {
        if(words.isNotEmpty()) {
            if (words.size > 1) {
                for (word in words) {
                    // Capitalize first letter
                    val firstLetter = word.substring(0, 1)
                    // Get remaining letter
                    val remainingLetters = word.substring(1)
                    modifiedString += if (needUpperCase) {
                        firstLetter.toUpperCase(Locale.ROOT) + remainingLetters + " "
                    } else {
                        firstLetter.toLowerCase(Locale.ROOT) + remainingLetters + " "
                    }
                }
            } else {
                modifiedString = words[0]
            }
        }
    }else{
        for ((i,word) in words.withIndex()) {
            // Capitalize first letter
            val firstLetter = word.substring(0, 1)
            // Get remaining letter
            val remainingLetters = word.substring(1)

            modifiedString += if(i==0) {
                if (needUpperCase) {
                    firstLetter.toUpperCase(Locale.ROOT) + remainingLetters + " "
                } else {
                    firstLetter.toLowerCase(Locale.ROOT) + remainingLetters + " "
                }
            }else{
                firstLetter.toLowerCase(Locale.ROOT) + remainingLetters + " "

            }
        }

    }
    return modifiedString
}
package com.app.okra.utils

class MessageConstants {

    class Errors {

        companion object {

            val please_fill_hyper_values = "Please fill the hyper or hypo blood glucose value to save."
            val hypo_cant_be_greater = "Hypo can't be greater than hyper."
            val please_enter_to_date = "Please enter to date."
            val please_enter_from_date = "Please enter from date."

            const  val please_select_protein="Please select protein"
            const  val please_select_fat="Please select fat"
            const  val please_select_carbs="Please select carbs"
            const  val please_select_calories="Please select calories"
            const  val please_select_date = "Please select date."
            const  val please_select_image = "Please select image."
            const  val please_select_food_type = "Please select food type."
            const  val selected_time_should_not_be = "Selected time should not be lesser than current time."
            const  val invalid_age = "Age must be between 35 and 150"
            const val please_enter_decline_reason = "Please enter decline reason."
            const val please_enter_message = "Please enter message"
            const val enter_group_name = "Enter group name"
            const val price_validation = "Price can be equal or less than 1000$"
            const val enter_price = "Please enter price"
            const val enter_minimum_attendies = "Enter minimum 2 attendees"
            const val enter_group_details = "Please enter group details"
            const val select_interest = "Please select interest"
            const val enter_no_of_attendies = "Please enter no of attendees"
            const val select_end_time = "Please select end time"
            const val select_time ="Please select time"
            const val select_location = "Please enter location"
            const val select_start_time = "Please select start time"
            const val enter_group_activity_name = "Please enter activity name"
            const val please_choose_any_option ="Please choose any option."
            const val please_choose_interest ="Please choose interest"
            const val ep_enter_disposal_issue_name = "Please enter disposal issue name."
            const val ep_enter_environmental_review = "Please enter environmental review name."
            const val ep_enter_environmental_disposal_location= "Please enter disposal location."
            const val ep_enter_disposal_process= "Please enter disposal process."
            const val ep_please_choose_any_option= "Please choose any option."
            const val please_choose_people_involved= "Please choose people involved."
            const val please_enter_witness_details= "Please enter witness details."
            const val please_count_message= "You can only add 5 emergency plan."

            const val enter_relationship = "Please enter relationship"
            const val enter_address = "Please enter address"
            const val enter_name = "Please enter name"
            const val upload_failed = "Upload failed."
            const val enter_further_information = "Please enter further information"
            const val enter_classification = "Please enter classification"
            const val enter_disability_name = "Please enter disability name"
            const val enter_about_me = "Please enter about yourself"
            const val enter_postal_address = "Please enter postal address"
            const val enter_residential_address = "Please enter residential address"
            const val enter_location = "Please enter location"
            const val enter_phone_number = "Please enter phone number"
            const val enter_language = "Please enter language"
            const val enter_date_of_birth = "Please select date of birth"
            const val enter_first_name = "Please enter first name."
            const val enter_info = "Please enter information."
            const val enter_last_name = "Please enter last name."
            const val enter_email = "Please enter email."
            const val enter_detail="Please Enter Details"
            const val select_category="Please select Categories"
            const val enter_start_time="Please select start time"
            const val enter_end_time="Please select end time"
            const val choose_tag = "Please choose a tag."
            const val no_url_found = "No url found."
            const val please_describe_needs = "Please describe what you need."
            const val please_enter_title = "Please enter title"
            const val please_enter_description = "Please enter description"
            const val enter_pass = "Please enter password."
            const val enter_current_password = "Please enter current password."
            const val enter_confirm_pass = "Please enter confirm password."
            const val invalid_current_pass = "Invalid current password"

            const val invalid_email_pass = "Invalid Email ID or Password"
            const val invalid_email = "Invalid Email ID"
            const val invalid_pass = "Invalid Password"
            const val invalid_confirm_pass = "Invalid Confirm Password"
            const val password_mismatch = "You must type the same password each time"
            const val same_password_issue = "Current password & New password couldn't be same"
            const val invalid_phone = "Invalid Phone No."
            const val invalid_data = "Invalid data."
            const val fb_login_failed = "Facebook Login Failed."
            const val an_error_occurred = "An error occurred. Please try again later."
            const val please_select_rating = "Please select rating."
            const val please_select_rating_tags = "Please select rating tags."
            const val no_supporter_found = "No supporter found."

            const val enter_otp = "Please enter OTP."
            const val invalid_otp = "Invalid verification code"
            const val user_already_exist = "User already exist."
            const val user_not_exist = "User does not exist."
            const val user_not_confirmed = "User is not confirmed"
            const val network_issue = "Network Issue."
            const val network_issue_try_again = "Network Issue. please try again later."
            const val please_agree = "Please agree to our terms and conditions."
            const val invalid_pass_message = "Password must be at least 8 characters and" +
                    " must include 1 uppercase letter," +
                    " 1 lowercase letter, 1 special character and 1 numeric digit."

            const val ndis_number = "Please enter ndis number."
            const val ndis_start_date = "Please choose plan start date."
            const val ndis_end_date = "Please choose plan end date."
            const val ndis_total_plan_amount= "Please enter total plan amount."
            const val ndis_total_core= "Please enter total core."
            const val ndis_allocated_core= "Please enter allocated core."
            const val ndis_total_capacity_building= "Please enter total capacity building."
            const val ndis_allocated_cb= "Please enter allocated CB."
            const val ndis_total_support_coordinator= "Please enter total support coordinator."
            const val ndis_allocated_sc= "Please enter allocated SC."
            const val ndis_stated_support =  "Please enter stated supports."
            const val ndis_date_updated =  "Please choose date updated."
            const val ndis_time_updated =  "Please choose time updated."
            const val ndis_amount_allocated=  "Please enter amount allocated to okra."
            const val enter_valid_phone_number=  "Please enter valid phone number."

            const val pDetails_amount_name=  "Please enter account name."
            const val pDetails_bank_state_branch=  "Please enter bank state branch."
            const val pDetails_account_no=  "Please enter account no."
            const val pDetails_fund_type=  "Please enter fund type."
            const val pDetails_super_fund_name=  "Please enter super fund name."
            const val pDetails_fund_abn=  "Please enter fund ABN."
            const val pDetails_fund_usi=  "Please enter fund USI."
            const val pDetails_member_name=  "Please enter member name."

            const val ndisGoal_message=  "Please enter ndis goal."
        }
    }

    class Messages {

        companion object {
            var sessionMsg="Your login session has been expired."

            val msg_account_deleted_successfully: String ="Account deleted successfully."
            val file_downloaded_successfully = "File downloaded successfully"
            val addtional_notes_not_allowed = "Additional Notes is not allowed to be empty"
            val medication_updated_successfully = "Medicine updated successfully"
            val alert_msg_sync = "Data of this Okra device is already synced with this user's database."
            val message_unit_interchange = "You can't select this option."
            val test_already_added = "Test already added."
            val test_fetched_successfully = "Test fetched successfully"
            val you_can_only_pair = "You can only pair the Okra pro devices with this application."
            const val do_you_want_to = "Do you want to connect to this device?"
            const val you_cant_add_more= "You can't add more than 10 images."
            const val no_serving_available = "No serving available."
            const val please_enable_your_bluetooth=  "Please enable your bluetooth and retry."
            const val internet_connection_required ="Internet connection required."
            const val msg_storage_permission ="You have to give storage permission"
            const val msg_camera_permission ="You have to give camera permission"
            const val msg_are_you_sure="Are you sure you want to delete this medication?"
            const val msg_are_you_sure_deleteAccount="Are you sure you want to delete your account?"
            const val msg_location_permission ="You have to give location permission"
            const val we_have_sent = "We have resent the verification code on your Registered Email Id"
            const val one_to_one_message = "Do you want to bring your own supporter?"
            const val ot_sent = "A verification code has been sent to your mail address."
            const val user_not_verified = "User is not verified. please verify it first."
            const val password_change_successfully = "Password change successfully."
            const val user_not_registered = "User not registered."
            const val invalid_code = "Invalid OTP."
            const val limit_exceed = " Attempt Limit exceeded. Please try again later."
            const val exit_app_message = "Are you sure, you want to exit the app?"
            const val delete_group_message = "Are you sure, you want to delete this group?"
            const val start_activity_message = "Are you sure, you want to start this activity?"
            const val already_scanning = "Already scanning"
            const val stop_activity_message = "Are you sure, you want to stop this activity?"
            const val delete_activity_message = "Are you sure, you want to delete this activity?"
            const val decline_request_message = "Are you sure, you want to decline this activity?"
            const val cancel_request_message = "Are you sure, you want to cancel this activity?"
            const val reject_request_message = "Are you sure, you want to reject this request?"
            const val confirmation_end_time_message = "Is this the actual finish time for this activity or you need to edit the finish time?"
            const val finish_activity_message = "Are you sure you want to finish the activity as once finished you will no longer be able to edit/add any voice note?"
            const val logout_message = "Are you sure you want to logout from the app?"
            const val bluetooth_turn_on_permission = "Allow Okra to turn on the Bluetooth?"
            const val please_turn_on_your_location = "Please turn on your location?"
            const val location_permission_deny_text = "Location permission is required for this feature?"
            const val work_in_progress = "Work in progress."
            const val no_data_available = "No data available."
            const val no_app_found = "No app found to open downloaded file."
            const val no_device_available = "No device found."
            const val unsaved_meal_data = "You have unsaved meal details. These meal details will be lost."


        }
    }
    class BleMessages {

        companion object {
            var bleScanCallback_can_not_be_null ="BleScanCallback can not be Null!"
            var bleGattCallback_can_not_be_null ="BleGattCallback can not be Null!"
            var bleNotifyCallback_can_not_be_null ="BleNotifyCallback can not be Null!"
            var bleIndicateCallback_can_not_be_null ="BleIndicateCallback can not be Null!"
            var bleWriteCallback_can_not_be_null ="BleWriteCallback can not be Null!"
            var bleReadCallback_can_not_be_null = "BleReadCallback can not be Null!"
            var bleRssiCallback_can_not_be_null = "BleRssiCallback can not be Null!"
            var bleMTUCallback_can_not_be_null = "BleMtuChangedCallback can not be Null!"
            var device_not_connect ="This device not connect!"
            var bluetooth_not_enable ="Bluetooth not enable!"
            var bleScanAndConnectCallback_can_not_be_null = "BleScanAndConnectCallback can not be Null!"
            var beCareful_current_thread = "Be careful: currentThread is not MainThread!"
            var beCareful_data_length = "Be careful: data's length beyond 20! Ensure MTU higher than 23, or use spilt write!"
            var not_Found_Device_Exception_Occurred = "Not Found Device Exception Occurred!"
            var data_is_null = "data is Null!"
            var requiredMtu_should_higher = "requiredMtu should higher than 23 !"
            var requiredMtu_should_higher_2 = "requiredMtu should lower than 512 !"
            var scanResult_not_null = "scanResult can not be Null!"


        }
    }

    class ProgressBar {

        companion object {
            const val verifying = "Verifying."
            const val processing = "Processing."
            const val uploading = "Uploading."
            const val logging_in = "Logging In."


        }
    }

    class ResponseMessage {

        companion object {
            const val user_not_confirmed = "User is not confirmed"
            const val user_not_exist = "User does not exist"
            const val incorrect_user_pass = "Incorrect username or password"
            const val account_already_exist = "An account with the given email already exists."
            const val not_a_registered_user = "Cannot reset password for the user as there is no registered/verified email or phone_number"
            const val invalid_verification_code = "Invalid verification code provided, please try again."
            const val limit_exceed = "Attempt limit exceeded, please try after some time."
        }
    }

}
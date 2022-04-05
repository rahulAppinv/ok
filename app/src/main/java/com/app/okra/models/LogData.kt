package com.app.okra.models

class TestLogFilter {
    var fromDate: String? = null
    var toDate: String? = null
    var displayAll: Boolean? = null
    var beforeMeal: Boolean? = null
    var afterMeal: Boolean? = null
    var postMed: Boolean? = null
    var postWorkout: Boolean? = null
    var controlSolution: Boolean? = null
    var isFilterApplied :Boolean=false


         fun checkFilterApplied() {
            isFilterApplied = !(fromDate.isNullOrEmpty()
                    && toDate.isNullOrEmpty()
                    && (displayAll==null || !displayAll!!)
                    && (beforeMeal==null || !beforeMeal!!)
                    && (afterMeal==null || !afterMeal!!)
                    && (postMed==null || !postMed!!)
                    && (postWorkout==null || !postWorkout!!)
                    && (controlSolution==null || !controlSolution!!))
        }

}

class MealLogFilter {
    var fromDate: String? = null
    var toDate: String? = null
    var displayAll: Boolean? = null
    var today: Boolean? = null
    var thisWeek: Boolean? = null
    var thisMonth: Boolean? = null
    var isFilterApplied :Boolean=false


        fun checkFilterApplied() {
            isFilterApplied = !(fromDate.isNullOrEmpty()
                    && toDate.isNullOrEmpty()
                    && (displayAll==null || !displayAll!!)
                    && (today==null || !today!!)
                    && (thisWeek==null || !thisWeek!!)
                    && (thisMonth==null || !thisMonth!!))

        }

}
class MedicationLogFilter {
    var fromDate: String? = null
    var toDate: String? = null
    var all: Boolean? = null
    var pills: Boolean? = null
    var mg: Boolean? = null
    var ml: Boolean? = null
    var isFilterApplied :Boolean=false


        fun checkFilterApplied() {
            isFilterApplied = !(fromDate.isNullOrEmpty()
                    && toDate.isNullOrEmpty()
                    && (all==null || !all!!)
                    && (pills==null || !pills!!)
                    && (ml==null || !ml!!))
        }

}
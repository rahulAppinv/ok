package com.app.okra.data.repo

import com.app.okra.data.network.ApiService
import com.app.okra.data.network.BaseRepo

class DashboardRepoImpl constructor(var apiService: ApiService ) : BaseRepo(apiService), DashboardRepo {


}
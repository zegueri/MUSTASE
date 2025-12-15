package com.example.mustase.data

import retrofit2.Response
import retrofit2.http.GET

@Suppress("unused")
interface UserWebService {
    @GET("/sync/v9/user/")
    suspend fun fetchUser(): Response<User>
}

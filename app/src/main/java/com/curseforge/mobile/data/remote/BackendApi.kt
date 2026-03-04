package com.curseforge.mobile.data.remote

import com.curseforge.mobile.data.model.FileInfoResponse
import com.curseforge.mobile.data.model.SearchResponse
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface BackendApi {
    @GET("api/search")
    suspend fun searchAddons(
        @Query("q") query: String,
        @Query("page") page: Int,
        @Query("version") version: String? = null
    ): SearchResponse

    @GET("api/file/{fileId}")
    suspend fun fileInfo(@Path("fileId") fileId: Long): FileInfoResponse
}

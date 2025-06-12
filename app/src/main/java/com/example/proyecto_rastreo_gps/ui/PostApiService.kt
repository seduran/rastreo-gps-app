package com.example.proyecto_rastreo_gps.ui

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Path

interface PostApiService {
    @GET("/api/client/{ru}")
    suspend fun getListaClientes(@Path("ru") ru:String): Response<ListaModelResponse>

    @GET("/api/location/{codcli}")
    suspend fun getUbicacionPersona(@Path("codcli") codCli:String): Response<UbicacionModelResponse>

    @GET("/api/location/all/{ru}")
    suspend fun getUbicacionTodos(@Path("ru") ru:String): Response<UbicacionTodosResponse>

    @Headers("Content-type: application/json; charset=UTF-8")
    @POST("api/client")
    suspend fun addDatos(@Body obj:PersonaModelRequest): Response<PersonaModelResponse>

    @Headers("Content-type: application/json; charset=UTF-8")
    @POST("api/location")
    suspend fun addDatosUbicacion(@Body obj:UbicacionModelRequest): Response<PersonaModelResponse>

    @Headers("Content-type: application/json; charset=UTF-8")
    @PUT("api/client/{codcli}")
    suspend fun updatePost(
        @Path("codcli") postId: String,
        @Body postData: PersonaModelRequest
    ): Response<PersonaModelResponse>

    @DELETE("api/client/{codcli}/{ru}")
    suspend fun deletePost(
        @Path("codcli") codCli: String,
        @Path("ru") ru: String,
        ): Response<PersonaModelResponse>
}
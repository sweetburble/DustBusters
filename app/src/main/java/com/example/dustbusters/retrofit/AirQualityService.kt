package com.example.dustbusters.retrofit

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * 인터페이스를 작성하면 레트로핏 라이브러리가 인터페이스에 정의된 API 엔드포인트들을 자동으로 구현한다.
 */
interface AirQualityService {

    @GET("nearest_city") // https://api.airvisual.com/v2/nearest_city
    fun getAirQualityData(@Query("lat") lat: String, @Query("lon") lon: String,
                          @Query("key") key: String) : Call<AirQualityResponse>
}
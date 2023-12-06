package com.bandi.dustbusters.retrofit

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * 인터페이스를 작성하면 레트로핏 라이브러리가 인터페이스에 정의된 API 엔드포인트들을 자동으로 구현한다.
 */
interface AirNewsService {
    @GET("getMinuDustFrcstDspth") // https://apis.data.go.kr/B552584/ArpltnInforInqireSvc/getMinuDustFrcstDspth
    fun getAirNewsData(@Query("serviceKey") serviceKey: String, @Query("returnType") returnType: String,
                          @Query("searchDate") searchDate: String, @Query("ver") ver: String, @Query("informCode") informCode: String) : Call<AirNewsResponse>
}
package com.example.dustbusters.retrofit

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

/**
 * 레트로핏 객체를 생성하는 getInstance() 함수를 구현한다.
 */
class RetrofitConnection {

    // 레트로핏 객체를 딱 하나만 생성하는 singleton 패턴이다
    companion object {
        // API 서버의 주소가 BASE_URL이 된다
        private const val BASE_URL = "https://api.airvisual.com/v2/"
        private var INSTANCE : Retrofit? = null

        fun getInstance() : Retrofit {
            if (INSTANCE == null) { // 레트로핏 객체가 null인 경우에만 생성
                INSTANCE = Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    // Gson 컨버터 팩토리는 서버에서 온 JSON 응답을 AirQualityResponse 데이터 클래스로 변환한다
                    .build()
            }
            return INSTANCE!!
        }
    }
}
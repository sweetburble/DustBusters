package com.bandi.dustbusters.retrofit

data class AirQualityResponse(
    val `data`: Data,
    val status: String
) {
    data class Data( // 이런 식으로 중첩 클래스 선언이 가능하다
        val city: String,
        val country: String,
        val current: Current,
        val location: Location,
        val state: String
    ) {
        data class Current(
            val pollution: Pollution,
            val weather: Weather
        ) {
            data class Pollution(
                val aqicn: Int,
                val aqius: Int, // 미국 기준 대기질 지수이다. 이 값을 사용할 것이다
                val maincn: String,
                val mainus: String,
                val ts: String
            )

            data class Weather(
                val hu: Int,
                val ic: String,
                val pr: Int,
                val tp: Int,
                val ts: String,
                val wd: Int,
                val ws: Double
            )
        }

        data class Location(
            val coordinates: List<Double>,
            val type: String
        )
    }
}
package com.bandi.dustbusters

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import com.bandi.dustbusters.databinding.ActivityNewsBinding
import com.bandi.dustbusters.retrofit.AirNewsResponse
import com.bandi.dustbusters.retrofit.AirNewsService
import com.bandi.dustbusters.retrofit.AirQualityResponse
import com.bandi.dustbusters.retrofit.AirQualityService
import com.bandi.dustbusters.retrofit.NewsRetrofitConnection
import com.bandi.dustbusters.retrofit.RetrofitConnection
import com.bumptech.glide.Glide
import com.bumptech.glide.load.model.GlideUrl
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

class NewsActivity : AppCompatActivity() {

    lateinit var binding: ActivityNewsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNewsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val nowTime = LocalDateTime.now()
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        val searchDate: String
        if (nowTime.hour <= 5) {
            // 오전 5시 이전이면 어제 날짜를 저장
            searchDate = nowTime.minusDays(1).format(formatter)
        } else {
            // 그렇지 않으면 오늘 날짜를 저장
            searchDate = nowTime.format(formatter)
        }

//        getAirNewsData("O3")
        getAirNewsData(searchDate, "PM10")
        getAirNewsData(searchDate, "PM25")

    }

    private fun getAirNewsData(searchDate : String, informCode : String) {
        // 레트로핏 객체를 이용해, AirNewsService 인터페이스의 구현체를 가져올 수 있다
        val retrofitAPI = NewsRetrofitConnection.getInstance().create(AirNewsService::class.java)

        retrofitAPI.getAirNewsData(
            "{API 키가 들어갑니다}",
            "json",
            searchDate,
            "1.1",
            informCode // PM25, PM10, O3에 대한 애니메이션 이미지는 따로따로 요청해야 한다
        ).enqueue(object : Callback<AirNewsResponse> {
            override fun onResponse(
                call: Call<AirNewsResponse>,
                response: Response<AirNewsResponse>
            ) {
                // 정상적인 Response가 왔다면 UI를 업데이트한다
                if (response.isSuccessful) {
                    Toast.makeText(this@NewsActivity,
                        "최신 뉴스 업데이트 완료!", Toast.LENGTH_SHORT).show()

                    // response.body()가 null이 아니면, updateNewsUI()
                    response.body()?.let { updateNewsUI(it, informCode) }
                } else {
                    Toast.makeText(this@NewsActivity,
                        "뉴스 업데이트가 실패했습니다...", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<AirNewsResponse>, t: Throwable) {
                t.printStackTrace()
            }
        })
    }

    /**
     * 가져온 기상청 뉴스 정보를 바탕으로 화면을 업데이트한다.
     */
    private fun updateNewsUI(airNewsData: AirNewsResponse, informCode: String) {

        val News = airNewsData.response.body.items[0]

        // 뉴스 발표 시간 갱신
        binding.newsTime.text = News.dataTime.toString()
        // 뉴스 예보 개황 갱신
        binding.newsOverall.text = News.informOverall.toString()

        when (informCode) {
            "PM10" -> {
                Glide.with(this)
                    .asGif()
                    .load(News.imageUrl7)
                    .into(binding.imgPM10)
            }
            "PM25" -> {
                Glide.with(this)
                    .asGif()
                    .load(News.imageUrl8)
                    .into(binding.imgPM25)
            }
            "O3" -> {
                if (News.imageUrl9 != "https://www.airkorea.or.kr/file/proxyImage?fileName=") { // 애니메이션 이미지를 제공한다면
                    binding.textO3.text = ""
                    Glide.with(this)
                        .asGif()
                        .load(News.imageUrl9)
                        .into(binding.imgO3)
                } else {
                    binding.textO3.text = "오존예보는 매년 4월15일 ~ 10월15일 까지만 발표됩니다."
                }
            }
            else -> {
                Toast.makeText(this@NewsActivity,
                    "예보 이미지가 정상적으로 로딩되지 못했습니다..", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private val callback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            finish()
        }
    }
}
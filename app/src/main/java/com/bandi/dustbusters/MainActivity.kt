package com.bandi.dustbusters

import android.Manifest
import android.app.Activity
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.LocationManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.bandi.dustbusters.databinding.ActivityMainBinding
import com.bandi.dustbusters.retrofit.AirQualityResponse
import com.bandi.dustbusters.retrofit.AirQualityService
import com.bandi.dustbusters.retrofit.RetrofitConnection

import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.lang.Exception
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.*

class MainActivity : AppCompatActivity() {

    lateinit var binding : ActivityMainBinding

    // 런타임 권한 요청 시, 필요한 요청 코드
    private val PERMISSIONS_REQUEST_CODE = 100
    
    // 요청할 권한 리스트
    var REQUIRED_PERMISSIONS = arrayOf(
        Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)

    // 위치 서비스 요청 시, 필요한 (ActivityResult) 런처
    lateinit var getGPSPermissionLauncher: ActivityResultLauncher<Intent>

    // 위도와 경도를 가져올 때, 필요하다
    lateinit var locationProvider: LocationProvider

    // 위도와 경도를 클래스 객체 변수로 저장한다
    var latitude : Double = 0.0
    var longitude : Double = 0.0

    val startMapActivityResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult(),
        object : ActivityResultCallback<ActivityResult> {
            override fun onActivityResult(result: ActivityResult) {
                if (result?.resultCode ?: 0 == Activity.RESULT_OK) {
                    // 지도 페이지(MapActivity.kt)에서는 위도와 경도를 반환한다. 이 값을 객체 변수에 각각 저장한다
                    latitude = result?.data?.getDoubleExtra("latitude", 0.0) ?: 0.0
                    longitude = result?.data?.getDoubleExtra("longitude", 0.0) ?: 0.0
                    updateUI() // 얻어온 위도와 경도로 updateUI()를 실행하여 미세먼지 농도를 구한다
                }
            }
        })

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        checkAllPermissions() // 모든 권한이 허용되었는지 확인
        updateUI()

        setRefreshButton()
        setFabMap() // 현재 위도와 경도 정보를 담아 지도 페이지로 보내는 함수
        setFabNews() // 대기질 예보 뉴스 페이지로 보내는 함수
    }

    private fun setFabNews() {
        binding.fabNews.setOnClickListener {
            val intent = Intent(this, NewsActivity::class.java)
            startActivity(intent)
        }
    }

    /**
     * startMapActivityResult 변수를 launch()하면 지도 페이지로 이동하고, 콜백 함수로 등록해둔 onActivityResult가 실행된다.
     */
    private fun setFabMap() {
        binding.fab.setOnClickListener {
            val intent = Intent(this, MapActivity::class.java)
            intent.putExtra("currentLat", latitude)
            intent.putExtra("currentLng", longitude)
            startMapActivityResult.launch(intent)
        }
    }

    private fun setRefreshButton() {
        binding.btnRefresh.setOnClickListener {
            updateUI()
        }
    }

    /**
     * 위도와 경도 정보를 LocationProvider를 이용해 가져온다.
     * 위도와 경도 각각은 getLocationLatitude()와 getLocationLongitude()를 사용한다.
     */
    private fun updateUI() {
        locationProvider = LocationProvider(this@MainActivity)

        // 위도와 경도 정보를 가져온다
        if (latitude == 0.0 || longitude == 0.0) {
            latitude = locationProvider.getLocationLatitude()
            longitude = locationProvider.getLocationLongitude()
        }

        if (latitude != 0.0 || longitude != 0.0) {
            // 1. 현재 위치를 가져오고, UI 업데이트 (getFromLocation이 Deprecated이기 때문에 GeocodeListener 구현)

            if (Build.VERSION.SDK_INT < 33) { // SDK 버전이 33보다 작다면, 그대로
                // 1-1. 현재 위치 가져오기
                val address = getCurrentAddress(latitude, longitude)
                // 1-2. 주소가 null이 아니여야, UI 업데이트
                address?.let {
                    binding.tvLocationTitle.text = "${it.thoroughfare}" // 예시 : 쌍문동
                    binding.tvLocationSubtitle.text = "${it.countryName} ${it.adminArea}"
                    // 예시 : 대한민국 서울특별시
                }
            } else { // SDK 버전이 33보다 같거나 크다면
                val geocoder = Geocoder(this, Locale.getDefault())
                var address : Address? = null
                val geocodeListener = @RequiresApi(33) object : Geocoder.GeocodeListener {
                    override fun onGeocode(addresses: MutableList<Address>) {
                        // 주소 리스트에서 address를 하나씩 꺼낸 다음에, 처음으로 thoroughfare에 값이 있는 address로 설정한다
                        for (add in addresses) {
                            if (add.thoroughfare != null && add.thoroughfare.length > 0) {
                                address = add;
                                break;
                            }
                        }
                        address?.let {
                            binding.tvLocationTitle.text = "${it.thoroughfare}" // 예시 : 쌍문동
                            binding.tvLocationSubtitle.text = "${it.countryName} ${it.adminArea}"
                            // 예시 : 대한민국 서울특별시
                        }
                    }

                    override fun onError(errorMessage: String?) {
                        address = null
                        Toast.makeText(this@MainActivity,
                            "주소가 발견되지 않았습니다.", Toast.LENGTH_LONG).show()
                    }
                }
                geocoder.getFromLocation(latitude, longitude, 7, geocodeListener)
            }

            // 2. 현재 미세먼지 농도를 가져오고, UI 업데이트
            getAirQualityData(latitude, longitude)

        } else { // 위도, 경도 정보가 모두 없다면
            Toast.makeText(this@MainActivity,
                "위도, 경도 정보를 가져오지 못했습니다. 새로고침을 눌러주세요.", Toast.LENGTH_LONG).show()
        }
    }

    /**
     * 레트로핏 클래스를 이용하여 미세먼지 오염 정보를 가져온다.
     */
    private fun getAirQualityData(latitude: Double, longitude: Double) {
        // 레트로핏 객체를 이용해, AirQualityService 인터페이스의 구현체를 가져올 수 있다
        val retrofitAPI = RetrofitConnection.getInstance().create(AirQualityService::class.java)

        retrofitAPI.getAirQualityData(
            latitude.toString(),
            longitude.toString(),
            "{API 키가 들어갑니다}"
        ).enqueue(object : Callback<AirQualityResponse> { // enqueue() 함수에는 retrofit.Callback 인터페이스 구현체를 인수로 넘겨야 한다
            override fun onResponse(
                call: Call<AirQualityResponse>,
                response: Response<AirQualityResponse>
            ) {
                // 정상적인 Response가 왔다면 UI를 업데이트한다
                if (response.isSuccessful) {
                    Toast.makeText(this@MainActivity,
                        "최신 정보 업데이트 완료!", Toast.LENGTH_SHORT).show()

                    // response.body()가 null이 아니면, updateAirUI()
                    response.body()?.let { updateAirUI(it) }
                } else {
                    Toast.makeText(this@MainActivity,
                        "업데이트에 실패했습니다.", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<AirQualityResponse>, t: Throwable) {
                t.printStackTrace()
            }
        })
    }

    /**
     * 가져온 데이터 정보를 바탕으로 화면 업데이트
     */
    private fun updateAirUI(airQualityData : AirQualityResponse) {

        val pollutionData = airQualityData.data.current.pollution

        // 미세먼지 수치 갱신 (메인 화면 가운데 숫자)
        binding.tvCount.text = pollutionData.aqius.toString()

        // 측정 날짜와 시간을 얻고, 뷰에 갱신한다
        val dateTime = ZonedDateTime.parse(pollutionData.ts).withZoneSameInstant(
            ZoneId.of("Asia/Seoul")).toLocalDateTime() // ZonedDateTime 클래스를 이용하여, UTC 시간대를 한국 시간대로 변경

        val dataFormatter : DateTimeFormatter = // DateTimeFormatter.ofPattern() 함수를 통해서, 날짜 표기 형식을 지정했다
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")

        binding.tvCheckTime.text = dateTime.format(dataFormatter).toString()

        when (pollutionData.aqius) {
            in 0..50 -> {
                binding.tvTitle.text = "좋음"
                binding.imgBg.setImageResource(R.drawable.bg_good)
            }
            in 51..150 -> {
                binding.tvTitle.text = "보통"
                binding.imgBg.setImageResource(R.drawable.bg_soso)
            }
            in 151..200 -> {
                binding.tvTitle.text = "나쁨"
                binding.imgBg.setImageResource(R.drawable.bg_bad)
            }
            else -> {
                binding.tvTitle.text = "매우 나쁨"
                binding.imgBg.setImageResource(R.drawable.bg_worst)
            }
        }
    }

    /**
     * 지오코더 객체를 사용해서, 지오코딩을 할 수 있다.
     * GeoCoding(지오코딩) : 주소나 지명을 위도와 경도로 변환하거나, 위도와 경도를 주소나 지명으로 바꾸는 작업
     */
    fun getCurrentAddress(latitude : Double, longitude: Double) : Address? {
        val geocoder = Geocoder(this, Locale.getDefault())
        // Address 객체는 주소와 관련된 여러 정보를 가지고 있다.
        val addresses : List<Address>?

        addresses = try {
            // Geocoder 객체를 이용하여, 위도와 경도로부터 리스트를 가져온다
            geocoder.getFromLocation(latitude, longitude, 7)
        } catch (ioException : Exception) {
            Toast.makeText(this, "지오코더 서비스를 사용할 수 없습니다.", Toast.LENGTH_LONG).show()
            return null
        } catch (illegalArgumentException : IllegalArgumentException) {
            Toast.makeText(this, "잘못된 위도와 경도입니다.", Toast.LENGTH_LONG).show()
            return null
        }

        var address : Address? = null
        // 주소 리스트에서 address를 하나씩 꺼낸 다음에, 처음으로 thoroughfare에 값이 있는 address로 설정한다
        if (addresses != null && addresses.size != 0) {
            for (add in addresses) {
                if (add.thoroughfare != null && add.thoroughfare.length > 0) {
                    address = add;
                    break;
                }
            }
        } else { // 에러는 아니지만, 주소가 발견되지 않은 경우
            Toast.makeText(this, "주소가 발견되지 않았습니다.", Toast.LENGTH_LONG).show()
            return null
        }

        return address
    }

    private fun checkAllPermissions() {
        // 1. 사용자의 폰이 위치 서비스(GPS)가 켜져 있는지 확인
        if (!isLocationServicesAvailable()) {
            showDialogForLocationServiceSetting();
        } else { // 2. 런타임 앱 권한을 모두 허용했는지 확인
            isRuntimePermissionsGranted();
        }
    }

    fun isLocationServicesAvailable() : Boolean {
        val locationManager = getSystemService(LOCATION_SERVICE) as LocationManager

        // GPS 프로바이더나 네트워크 프로바이더(WiFi, 기지국으로 위치를 구함) 중에 하나가 있다면 true
        return (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
                || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER))
    }

    fun isRuntimePermissionsGranted() {
        // 위치 권한을 가지고 있는지 확인
        val hasFineLocationPermission = ContextCompat.checkSelfPermission(
            this@MainActivity, Manifest.permission.ACCESS_FINE_LOCATION
        )
        val hasCoarseLocationPermission = ContextCompat.checkSelfPermission(
            this@MainActivity, Manifest.permission.ACCESS_COARSE_LOCATION
        )
        if (hasFineLocationPermission != PackageManager.PERMISSION_GRANTED ||
            hasCoarseLocationPermission != PackageManager.PERMISSION_GRANTED) {
            // 둘 중에 하나라도 권한이 없다면 Permission을 요청한다
            ActivityCompat.requestPermissions(this@MainActivity,
                REQUIRED_PERMISSIONS, PERMISSIONS_REQUEST_CODE)
        }
    }

    /**
     * 권한 요청을 하고 난 후, 결과값은 액티비티에서 구현되어 있는 onRequestPermissionsResult()를 오버라이드 하여 처리한다.
     * 여기서 모든 권한이 허용되었는지 확인하고, 만약 허용되지 않은 권한이 있다면 앱을 종료한다.
     */
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSIONS_REQUEST_CODE && grantResults.size == REQUIRED_PERMISSIONS.size) {

            var checkResult = true // 모든 권한을 허용했는지 저장하는 변수

            // 모든 권한을 허용했는지 체크한다
            for (result in grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    checkResult = false
                    break
                }
            }
            if (checkResult) {
                // 모든 권한이 허용되었으므로, 위치값을 가져올 수 있다
                updateUI()

            } else {
                // 권한을 하나라도 거부했다면, 앱 종료
                Toast.makeText(this@MainActivity,
                    "권한이 거부되었습니다. 앱을 다시 실행하여 권한을 허용해주세요.", Toast.LENGTH_LONG).show()
                finish() // 매인 액티비티 종료
            }
        }
    }

    private fun showDialogForLocationServiceSetting() {
        // 먼저 ActivityResultLauncher를 설정한다 (startActivityForResult 대체)
        // 이 런처를 이용하여 "결과값을 반환해야 하는 인텐트"를 실행할 수 있다
        getGPSPermissionLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()) { result ->
            // 결과값을 받았을 때 로직
            if (result.resultCode == Activity.RESULT_OK) {
                // 사용자가 GPS를 활성화했는지 확인
                if (isLocationServicesAvailable()) {
                    isRuntimePermissionsGranted() // 런타임 권한을 허용했는지 확인
                } else {
                    // GPS를 활성화하지 않다면 앱 종료
                    Toast.makeText(this@MainActivity,
                        "위치 서비스를 사용할 수 없습니다. 위치 서비스를 설정해주세요.", Toast.LENGTH_LONG).show()
                    finish() // 매인 액티비티 종료
                }
            }
        }

        // 사용자에게 권한 허용 의사를 물어보는 AlertDialog 생성
        val builder : AlertDialog.Builder = AlertDialog.Builder(this@MainActivity)
        builder.setTitle("위치 서비스 비활성화") // 알림창 제목
        builder.setMessage("위치 서비스가 꺼져 있습니다. 서비스를 설정해야 앱을 사용할 수 있습니다.")
        builder.setCancelable(true) // 알림창 바깥을 터치하면, 창이 닫힌다
        builder.setPositiveButton("설정",
            DialogInterface.OnClickListener { dialog, id -> // 확인 버튼 설정
                val callGPSSettingIntent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                getGPSPermissionLauncher.launch(callGPSSettingIntent)
            })
        builder.setNegativeButton("취소",
            DialogInterface.OnClickListener { dialog, id ->  // 취소 버튼 설정
                dialog.cancel()
                Toast.makeText(this@MainActivity,
                    "기기에서 위치 서비스(GPS) 설정 후 사용해주세요.", Toast.LENGTH_SHORT).show()
                finish()
            })

        builder.create().show() // 알림창 생성 후, 보여주기
    }

}

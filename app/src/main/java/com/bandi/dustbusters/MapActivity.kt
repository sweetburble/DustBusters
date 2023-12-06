package com.bandi.dustbusters

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.bandi.dustbusters.databinding.ActivityMapBinding
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions

/**
 * OnMapReadyCallback 인터페이스를 구현한다.
 * onMapReady()는 지도를 사용할 준비가 되었을 때 실행된다.
 */
class MapActivity : AppCompatActivity(), OnMapReadyCallback {

    lateinit var binding : ActivityMapBinding

    private var mMap: GoogleMap? = null
    var currentLat: Double = 0.0 // MainActivity.kt에서 전달된 위도
    var currentLng: Double = 0.0 // MainActivity.kt에서 전달된 경도

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMapBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // MainActivity에서 intent로 전달한 값을 가져온다.
        currentLat = intent.getDoubleExtra("currentLat", 0.0)
        currentLng = intent.getDoubleExtra("currentLng", 0.0)

        // 구글 맵 객체의 생명주기를 관리하는 supportFragmentManager
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
        // getMapAsync()는 mapFragment에 OnMapReadyCallback 인터페이스를 등록해준다. 그래야 지도가 준비되면 onMapReady()가 자동으로 실행된다
        mapFragment?.getMapAsync(this)

        binding.btnCheckHere.setOnClickListener {
            mMap?.let { // mMap이 null이 아닌 경우, 이 코드 블록을 실행
                val intent = Intent()
                intent.putExtra("latitude", it.cameraPosition.target.latitude) // 버튼이 눌린 시점의 카메라 위치를 가져온다. 그리고 인텐트에 위도와 경도로 넣는다
                intent.putExtra("longitude", it.cameraPosition.target.longitude)
                setResult(Activity.RESULT_OK, intent) // MainActivity의 onActivityResult() 함수가 실행된다
                finish() // Map 액티비티를 종료한다.
            }
        }
    }

    /**
     * 지도가 준비되었을 때, 실행되는 콜백
     */
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        mMap?.let {
            val currentLocation = LatLng(currentLat, currentLng)
            it.setMaxZoomPreference(20.0f) // 지도 줌 최대값 설정
            it.setMinZoomPreference(7.0f) // 지도 줌 최소값 설정
            it.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 16f))
        }

        setMarker() // 마커를 설정하는 함수 호출

        // 플로팅 액션 버튼이 눌렸을 때는 현재 위도/경도 정보를 가져와서 지도의 위치를 움직이도록 한다
        binding.fabCurrentLocation.setOnClickListener {
            val locationProvider = LocationProvider(this@MapActivity)
            // 위도와 경도 정보를 가져온다
            val latitude = locationProvider.getLocationLatitude()
            val longitude = locationProvider.getLocationLongitude()

            mMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(latitude, longitude), 16f))
            setMarker()
        }
    }

    /**
     * 마커를 지도에 추가흔 함수. setOnCameraMoveListener()를 통해 지도가 움직일 때, 마커도 함께 움직이도록 세팅한다.
     */
    private fun setMarker() {
        mMap?.let {
            it.clear() // 지도에 있는 마커를 먼저 삭제
            val markerOptions = MarkerOptions()
            markerOptions.position(it.cameraPosition.target) // 마커의 위치 설정
            markerOptions.title("마커 위치") // 마커의 이름 설정
            val marker = it.addMarker(markerOptions) // 지도에 마커를 추가하고, 마커 객체를 반환

            it.setOnCameraMoveListener {
                marker?.let { marker ->
                    marker.setPosition(it.cameraPosition.target)
                }
            }
        }
    }

}
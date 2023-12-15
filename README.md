# DustBusters
### 미세먼지 측정 및 대기질 예보 안드로이드 애플리케이션 📱💨 <br><br><br>

https://github.com/sweetburble/DustBusters/assets/79733289/6f8f991f-ea80-4467-9577-4c323dd6116c

<br><br><br>

## 1. 개요 📝

**DustBusters**는 유저가 주변 환경의 미세먼지 수치를 실시간으로 측정하고, 기상청에서 제공하는 대기질 예보를 확인할 수 있는 안드로이드 애플리케이션입니다.    
이 애플리케이션은 공기질에 대한 신속한 정보 제공을 통해 사용자의 건강을 보호하는데 도움을 주는 것을 목표로 합니다. 🌏🏥<br><br>

**DustBusters**라는 이름은 미국의 유명한 영화 "Ghostbusters"에서 영감을 얻었습니다.  
영화에서 등장하는 고스트 버스터즈가 유령을 각종 과학 장비로 잡는 것처럼,  
우리도 최신 과학 기술 집약체인 스마트폰을 통해서 미세먼지를 적극적으로 대비하고 대처했으면 합니다! 👻➡️💨<br><br>

<img src="https://github.com/sweetburble/DustBusters/assets/79733289/fce4ba50-e2e1-4dbb-a321-d285dc8fd5f4" />

## 2. 사용한 기술 스택 🛠️

**DustBusters**는 다음과 같은 기술 스택을 사용하여 개발되었습니다:

- **안드로이드 스튜디오**: "DustBusters" 개발의 주요 플랫폼입니다.
- **Kotlin**: 평소에 관심있던 언어이기도 했고, 안드로이드 앱 개발에 대해서 공부하다 보니까, Kotlin에 대한 자료가 더 많고 더 장점이 있다고 판단되어, Java 대신 사용했습니다.
- **Android Jetpack**: 안드로이드 젯팩의 ViewBinding은 "DustBusters"의 UI를 편리하게 구성하는 데 사용되었습니다.
- **Retrofit 라이브러리**: 이 라이브러리는 **DustBusters**가 IQAir나, 한국환경공단과 같은 외부 서비스와 통신하기 위해 사용되었습니다.
- **AirVisual API, Google Map API**: AirVisual API는 GPS나 Google Map에서 좌표 정보를 받아서, 미세먼지 정보를 반환합니다. 
- **한국환경공단 에어코리아 OpenAPI**: 예보정보와 발생 원인 정보를 조회하는 대기질(미세먼지/오존) 예보를 제공합니다. 그 외에도 정말 다양한 API를 제공합니다.
- **Glide 라이브러리**: 미디어 디코딩, 메모리 및 디스크 캐싱, 리소스 풀링을 인터페이스로 래핑하는 Android용 오픈 소스 미디어 관리 및 이미지 로딩 프레임워크, GIF 이미지를 출력하는 데 좋은 성능을 자랑합니다.<br><br><br>

## 3. 주요 기능 및 특징 🛠️

### 3.1. 미세먼지 측정 📊

**DustBusters**는 스마트폰에 내장된 GPS를 활용하여 현재 나의 위치를 측정하거나, Google Map에서 원하는 위치를 선택합니다.  
그 다음, 받은 위치 정보를 AirVisual에 넘겨주고, AirVisual은 해당 지역의 최신 미세먼지 정보를 제공합니다.  
결국, 유저는 원하는 위치가 어느 곳이든 미세먼지 농도를 실시간으로 파악할 수 있습니다. 📍<br><br>
<img src="https://github.com/sweetburble/DustBusters/assets/79733289/bccf4264-0438-491f-af29-30f11ca4539b" width="30%" height="30%"/>
<img src="https://github.com/sweetburble/DustBusters/assets/79733289/04590608-1b07-4a61-8070-902ee62ad88a" width="30%" height="30%"/><br><br><br>

### 3.2. 대기질 예보 📡

**DustBusters**는 한국환경공단에서 제공하는 대기질 예보 데이터를 받아와 사용자에게 제공합니다.  
데이터는 매일 4회(오전5시, 오전 11시, 오후5시(17시), 오후11시(23시))에 19개 권역으로 발표되고 있습니다. 높은 가시성을 위해 예측모델 결과 애니메이션 이미지를 제공합니다.  
이를 통해 사용자는 미세먼지 농도가 높을 것으로 예상되는 시간과 장소를 미리 알 수 있습니다. ⏰🗺️<br><br>
![ezgif com-resize](https://github.com/sweetburble/DustBusters/assets/79733289/1b6af488-b5f3-45a4-a7d1-221778d77fea)
<br><br><br>

## 4. 기대 효과 🚀

**DustBusters**는 사용자에게 실시간 미세먼지,오존 정보를 제공하여, 미세먼지로 인한 건강 피해를 최소화하는데 도움을 줄 것으로 기대됩니다.  
또한, 이 애플리케이션은 미세먼지에 대한 인식과 경각심을 높이고, 개인의 건강 관리에 대한 중요성을 부각하는 데에 기여할 것입니다. 🌈🌿<br><br><br>

## 5. 결론 🎯

**DustBusters**는 실시간 미세먼지 측정 및 대기질 예보 서비스를 제공함으로써 사용자의 건강을 보호하는데 기여하고자 합니다.  
차후에는, 백그라운드에서 실행 중이면 매 시간마다 자동으로 미세먼지 정보를 갱신하다가 농도가 나빠지면 푸시 메시지가 간다던지,  
아니면 좀 더 환경에 대한 다양한 정보를 제공하는 기능을 추가하고 싶습니다.  
앞으로도 사용자의 건강을 위해 **DustBusters**는 더욱 발전하고 개선될 것입니다. 💪🚀<br><br><br>

## 6. 참고 자료 📚

1. "Joyce의 안드로이드 앱 프로그래밍", 홍정아 지음  
2. "https://www.iqair.com/ko/", AirVisual API  
3. "https://www.data.go.kr/data/15073861/openapi.do#/tab_layer_detail_function", 한국환경공단_에어코리아_대기오염정보
4. "https://sweetburble.tistory.com/194", 그 외 참고 자료 정리



const script = document.createElement('script');
script.src = 'https://openapi.map.naver.com/openapi/v3/maps.js?ncpClientId=ip45oqmru1&libraries=services';
script.async = true;
script.onload = initializeMapAndAddMarker; // 네이버 지도 API 로드 후 지도 초기화 및 마커 추가
document.head.appendChild(script);

// 네이버 지도 초기화 및 마커 추가 함수
function initializeMapAndAddMarker() {
    // 지도를 생성합니다. 여기서 'map'은 지도를 표시할 HTML 요소의 id입니다.
    const map = new naver.maps.Map('map', {
        center: new naver.maps.LatLng(35.5665, 127.5), // 초기 지도 중심 위치 37.5665, 126.9780
        zoom: 7 // 초기 지도 줌 레벨
    });
    
	const marker = new naver.maps.Marker({
	    position: new naver.maps.LatLng(37.3595704, 127.105399),
	    map: map
	});
	



    // DB에서 위치 데이터를 가져와 마커를 추가하는 함수 호출
   /* fetchLocationDataAndAddMarkers(map);*/
}

// DB에서 위치 데이터를 가져와 마커를 추가하는 함수
/*function fetchLocationDataAndAddMarkers(map) {
    // 서버로부터 위치 데이터를 가져오는 비동기 요청을 보냅니다.
    // 이 부분은 실제로는 서버와의 통신을 통해 데이터를 가져오는 로직이어야 합니다.
    // 여기서는 간단한 예제를 위해 임의의 위치 데이터를 사용합니다.
    fetch('') // 적절한 엔드포인트와 데이터 형식을 사용하세요.
        .then(response => response.json())
        .then(data => {
            // 가져온 위치 데이터를 기반으로 마커를 생성하고 지도에 추가합니다.
            data.forEach(location => {
                const marker = new naver.maps.Marker({
                    position: new naver.maps.LatLng(location.latitude, location.longitude),
                    map: map
                });
            });
        })
        .catch(error => console.error('Error fetching location data:', error));
}*/


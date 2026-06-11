import http from 'k6/http';
import {check, sleep} from 'k6';

/**
 * 장소(관광지, 숙소, 식당) 필터링 조회 API 부하 테스트
 * - 서버의 RegionClassifier 및 CategoryClassifier에 정의된 실제 DB 저장 명칭을 사용합니다.
 */

export const options = {
    stages: [
        {duration: '30s', target: 20},  // 30초 동안 VU 20명으로 점진적 증가
        {duration: '1m', target: 50},   // 1분 동안 VU 50명 유지
        {duration: '30s', target: 0},   // 30초 동안 점진적 감소
    ],
    thresholds: {
        http_req_duration: ['p(95)<1000'], // 검색 쿼리 특성상 1초 이내를 목표로 설정
    },
};
const BASE_URL = 'https://api.toleave.cloud';
// const BASE_URL = 'http://localhost:8080'; // 로컬 서버 주소


const REGIONS = [
    '서울', '인천', '대전', '대구', '광주', '부산', '울산', '세종',
    '경기', '강원', '충북', '충남', '경북', '경남', '전북', '전남', '제주'
];


const THEMES = [
    '체험 및 액티비티',
    '자연 속에서 힐링',
    '열정적인 쇼핑투어',
    '미식 여행 및 먹방 중심',
    '문화예술 및 역사탐방',
    '기타'
];

const KEYWORDS = ['공원', '산', '바다', '박물관', '호텔', '맛집'];
const ENDPOINTS = ['touristspots', 'accommodations', 'restaurants'];

export default function () {
    // 랜덤하게 엔드포인트 선택
    const endpoint = ENDPOINTS[Math.floor(Math.random() * ENDPOINTS.length)];

    // 랜덤하게 필터링 조건 생성
    const useRegion = Math.random() > 0.3;
    const useTheme = Math.random() > 0.5;
    const useKeyword = Math.random() > 0.7;

    let queryParams = [];
    if (useRegion) {
        const region = REGIONS[Math.floor(Math.random() * REGIONS.length)];
        queryParams.push(`regions=${encodeURIComponent(region)}`);
    }
    if (useTheme) {
        const theme = THEMES[Math.floor(Math.random() * THEMES.length)];
        queryParams.push(`themes=${encodeURIComponent(theme)}`);
    }
    if (useKeyword) {
        const keyword = KEYWORDS[Math.floor(Math.random() * KEYWORDS.length)];
        queryParams.push(`keyword=${encodeURIComponent(keyword)}`);
    }

    const page = Math.floor(Math.random() * 3) + 1; // 1~3 페이지
    queryParams.push(`page=${page}`);
    queryParams.push(`size=20`);

    const url = `${BASE_URL}/places/${endpoint}?${queryParams.join('&')}`;

    const res = http.get(url);

    check(res, {
        'is status 200': (r) => r.status === 200,
        'is success true': (r) => {
            try {
                return JSON.parse(r.body).success === true;
            } catch (e) {
                return false;
            }
        },
    });

    sleep(1);
}

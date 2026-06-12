import http from 'k6/http';
import {check, sleep} from 'k6';

/**
 * 게시판 목록 조회 API 부하 테스트
 * - 페이징과 정렬 조건이 포함된 목록 조회를 테스트합니다.
 */

export const options = {
    stages: [
        {duration: '10s', target: 20}, // 10초 동안 VU를 20명으로 늘림
        {duration: '20s', target: 50}, // 20초 동안 VU 50명 유지
        {duration: '10s', target: 0},  // 10초 동안 종료
    ],
    thresholds: {
        http_req_duration: ['p(95)<500'], // 95%의 요청이 500ms 이내여야 함
    },
};

const BASE_URL = 'https://api.toleave.cloud';
// const BASE_URL = 'http://localhost:8080'; // 로컬 서버 주소


export default function () {
    // 페이지 번호나 검색 조건을 랜덤하게 주어 실제 부하와 유사하게 구성할 수 있습니다.
    const page = Math.floor(Math.random() * 5); // 0~4 페이지 랜덤 조회
    const url = `${BASE_URL}/board?page=${page}&size=10&sort=createdAt,desc`;

    const res = http.get(url);

    check(res, {
        'is status 200': (r) => r.status === 200,
        'list count is correct': (r) => {
            try {
                const body = JSON.parse(r.body);
                return body.success === true;
            } catch (e) {
                return false;
            }
        },
    });

    sleep(1);
}

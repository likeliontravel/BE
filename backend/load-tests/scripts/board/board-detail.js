import http from 'k6/http';
import {check, sleep} from 'k6';

export const options = {
    stages: [
        {duration: '10s', target: 20},
        {duration: '20s', target: 50},
        {duration: '10s', target: 0},
    ],
};

// 존재하는 게시글 ID 목록
const boardIds = [2, 5, 7, 8];
const BASE_URL = 'https://api.toleave.cloud';
// const BASE_URL = 'http://localhost:8080'; // 로컬 서버 주소

export default function () {
    // 배열에서 랜덤하게 하나를 선택
    const randomBoardId = boardIds[Math.floor(Math.random() * boardIds.length)];
    const url = `${BASE_URL}/board/${randomBoardId}`;

    const res = http.get(url);

    check(res, {
        'is status 200': (r) => r.status === 200,
        'response time < 500ms': (r) => r.timings.duration < 500,
    });

    sleep(1);
}
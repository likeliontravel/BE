import http from 'k6/http';
import {check, sleep} from 'k6';

/**
 * 게시글 상세 조회 API에 대한 부하 테스트 스크립트입니다.
 *
 * 실행 명령어: k6 run load-tests/scripts/board-detail-test.js
 */

export const options = {
    // 1. 설정: 50명의 사용자가 동시에 접속한다고 가정합니다.
    vus: 50,
    duration: '30s', // 30초 동안 테스트를 진행합니다.
};

export default function () {
    // 2. 테스트 대상 URL (ID가 1인 게시글을 조회한다고 가정)
    // 실제 DB에 있는 ID로 변경하면 더 정확한 테스트가 가능합니다.
    const boardId = 1;
    const url = `http://localhost:8080/board/${boardId}`;

    const res = http.get(url);

    // 3. 검증: 응답이 성공(200)인지 확인합니다.
    check(res, {
        'is status 200': (r) => r.status === 200,
        'response time < 500ms': (r) => r.timings.duration < 500,
    });

    // 4. 대기: 실제 사용자가 글을 읽는 시간(1초)을 시뮬레이션합니다.
    sleep(1);
}

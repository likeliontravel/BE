package org.example.be.domain.group.invitation.util;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriUtils;

/**
 * 초대 링크 처리 결과에 따라 브라우저를 프론트엔드로 되돌려보낼 리다이렉트 URL을 만드는 헬퍼
 *
 * 초대 링크는 백엔드 도메인(api.toleave.cloud)로 진입하지만, 처리 결과는 반드시 프론트엔드 페이지(toleave.cloud)에서 끝나야 한다.
 *
 * 이 URL 규격은 프론트엔드와의 계약. 두 호출부
 * ({@code InvitationJoinController}, {@code CustomSuccessHandler}) 가
 * 각자 문자열을 조립하지 않고 이 클래스 한 곳에서만 만든다. 프론트엔드 라우트가 바뀌면 여기만 고친다.
 */
@Component
public class InviteRedirectHelper {

	// 가입 결과 쿼리파라미터 값
	public static final String JOINED_NEW = "true";
	public static final String JOINED_ALREADY = "already";

	// 끝에 슬래시가 없는 값이 전제 (https://toleave.cloud)
	@Value("${app.frontend-base-url}")
	private String frontendBaseUrl;

	// 프론트엔드 홈
	public String homeUrl() {
		return frontendBaseUrl + "/main";
	}

	// 프론트엔드 로그인 페이지
	public String loginUrl() {
		return frontendBaseUrl + "/login";
	}

	// 프론트엔드 그룹 상세 페이지 (가입 성공 또는 이미 멤버인 경우의 도착지)
	public String groupPageUrl(String groupName, String joined) {
		String encodedGroupName = UriUtils.encodePathSegment(groupName, StandardCharsets.UTF_8);
		return frontendBaseUrl + "/group/" + encodedGroupName + "?joined=" + joined;
	}

	// 초대 처리 실패 시 홈으로 보내며 사유 전달 ( errorCode는 ErrorCode.name() )
	public String inviteErrorUrl(String errorCode) {
		String encodedErrorCode = URLEncoder.encode(errorCode, StandardCharsets.UTF_8);
		// 홈 경로 재사용 (쿼리파라미터 이용해 사유를 보내준다)
		return homeUrl() + "?inviteError=" + encodedErrorCode;
	}

}
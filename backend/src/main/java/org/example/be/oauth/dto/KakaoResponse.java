package org.example.be.oauth.dto;

import java.util.Map;

public class KakaoResponse implements OAuth2Response {
	private final Map<String, Object> attributes;
	private final Map<String, Object> kakaoAccount;
	private final Map<String, Object> profile;

	public KakaoResponse(Map<String, Object> attributes) {
		this.attributes = attributes;
		this.kakaoAccount = extractMapFromAttributes(attributes, "kakao_account");
		this.profile = extractMapFromAttributes(kakaoAccount, "profile");
	}

	private Map<String, Object> extractMapFromAttributes(Map<String, Object> source, String key) {
		if (source == null) {
			return null;
		}
		Object obj = source.get(key);
		if (obj instanceof Map) {
			try {
				@SuppressWarnings("unchecked")
				Map<String, Object> map = (Map<String, Object>)obj;
				return map;
			} catch (ClassCastException e) {
				return null;
			}
		}
		return null;
	}

	@Override
	public String getProviderId() {
		return String.valueOf(attributes.get("id"));
	}

	@Override
	public String getProvider() {
		return "KAKAO";
	}

	@Override
	public String getEmail() {
		return kakaoAccount != null ? (String)kakaoAccount.get("email") : null;
	}

	@Override
	public String getName() {
		return profile != null ? (String)profile.get("nickname") : null;
	}

	@Override
	public String getProfileImage() {
		return kakaoAccount != null ? (String)kakaoAccount.get("profile_image") : null;
	}
}

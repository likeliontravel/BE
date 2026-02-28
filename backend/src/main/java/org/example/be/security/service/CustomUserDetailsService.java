// package org.example.be.security.service;
//
// import org.example.be.member.entity.Member;
// import org.example.be.member.service.MemberService;
// import org.example.be.security.config.SecurityUser;
// import org.springframework.security.core.userdetails.UserDetails;
// import org.springframework.security.core.userdetails.UserDetailsService;
// import org.springframework.security.core.userdetails.UsernameNotFoundException;
// import org.springframework.stereotype.Service;
//
// import lombok.RequiredArgsConstructor;
//
// /*
//  * 사용자의 정보를 DB 에서 가져오는 서비스 */
// @Service
// @RequiredArgsConstructor
// public class CustomUserDetailsService implements UserDetailsService {
// 	private final MemberService memberService;
//
// 	@Override
// 	public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
//
// 		Member member = memberService.findByEmail(email)
// 			.orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다."));
//
// 		return new SecurityUser(
// 			member.getId(),
// 			member.getEmail(),
// 			"",
// 			member.getName(),
// 			member.getAuthorities()
// 		);
// 	}
// }

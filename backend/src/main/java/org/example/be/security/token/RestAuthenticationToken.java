// package org.example.be.security.token;
//
// import org.springframework.security.authentication.AbstractAuthenticationToken;
// import org.springframework.security.core.GrantedAuthority;
//
// import java.util.Collection;
//
// public class RestAuthenticationToken extends AbstractAuthenticationToken {
//
//     private final Object principal;
//
//     private final Object credentials;
//
//     /*
//     * 권한 받은 후
//     * 권한을 가지고 있는 생성자 */
//     public RestAuthenticationToken(Collection<? extends GrantedAuthority> authorities, Object principal, Object credentials) {
//         super(authorities);
//         this.principal = principal;
//         this.credentials = credentials;
//         // 인증 객체가 인증 되었는지 설정
//         setAuthenticated(true);
//     }
//
//     /*
//     * 권한 받기 전
//     * 권한을 가지고 있지 못한 생성자 */
//     public RestAuthenticationToken(Object principal, Object credentials) {
//         super(null);
//         this.principal = principal;
//         this.credentials = credentials;
//         // 인증 객체가 인증 되었는지 설정
//         setAuthenticated(false);
//     }
//
//     @Override
//     public Object getCredentials() {
//         return this.credentials;
//     }
//
//     @Override
//     public Object getPrincipal() {
//         return this.principal;
//     }
// }

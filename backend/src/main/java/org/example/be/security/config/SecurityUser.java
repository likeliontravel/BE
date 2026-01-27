package org.example.be.security.config;

import java.util.Collection;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;

import lombok.Getter;

public class SecurityUser extends User {
	@Getter
	private long id;
	@Getter
	private String name;

	public SecurityUser(
		long id,
		String email,
		String password,
		String name,
		Collection<? extends GrantedAuthority> authorities
	) {
		super(email, password, authorities);
		this.id = id;
		this.name = name;
	}
}

package org.example.be.legacy.generaluser.domain;

import org.example.be.legacy.generaluser.dto.GeneralUserDTO;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "general_user")
public class GeneralUser {

	// General User table id
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	// General User 이메일
	@Column(nullable = false, length = 100, unique = true)
	private String email;

	// General User 비밀번호
	@Column(nullable = false, length = 300)
	private String password;

	// General User 이름
	@Column(nullable = false, length = 100)
	private String name;

	// General User 권한
	@Column(nullable = false)
	private String role;

	@Column(unique = true)
	private String userIdentifier;

	@PrePersist
	@PreUpdate
	public void generateUserIdentifier() {
		this.userIdentifier = "gen" + "_" + email;
	}

	public GeneralUserDTO mapToDTO() {
		GeneralUserDTO generalUserDTO = new GeneralUserDTO();
		generalUserDTO.setId(id);
		generalUserDTO.setEmail(email);
		generalUserDTO.setPassword(password);
		generalUserDTO.setName(name);
		generalUserDTO.setRole(role);
		generalUserDTO.setUserIdentifier(userIdentifier);
		return generalUserDTO;
	}
}

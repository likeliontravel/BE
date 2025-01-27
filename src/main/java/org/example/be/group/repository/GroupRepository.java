package org.example.be.group.repository;

import org.example.be.group.entitiy.Group;
import org.example.be.unifieduser.entity.UnifiedUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GroupRepository extends JpaRepository<Group, Long> {

    Optional<Group> findByGroupName(String groupName);

    Optional<List<Group>> findByMembersContaining(UnifiedUser user);
}

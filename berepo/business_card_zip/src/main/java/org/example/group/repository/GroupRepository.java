package org.example.group.repository;

import org.example.group.entity.Group;
import org.example.oauth.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface GroupRepository extends JpaRepository<Group, Integer> {

    // 특정 유저의 그룹 전체 조회
    List<Group> findAllByUser(User user);

    boolean existsByUserAndName(User user, String name);

    boolean existsByUserIdAndName(Integer userId, String groupName);

    List<Group> findAllByUserIdOrderByNameAsc(Integer userId);
}

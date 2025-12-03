package org.example.group.repository;

import org.example.group.entity.GroupMember;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public interface GroupMemberRepository extends JpaRepository<GroupMember, Integer> {

    // 특정 그룹에 포함된 모든 명함
    List<GroupMember> findByGroupId(Integer groupId);

    // 명함 중복 체크
//    boolean existsByGroupIdAndDigitalCardId(Integer groupId, Integer digitalCardId);
    boolean existsByGroupIdAndPaperCardId(Integer groupId, Integer paperCardId);

    // 특정 명함이 속한 모든 그룹 ID 조회
    //List<GroupMember> findByDigitalCardId(Integer cardId);
    List<GroupMember> findByPaperCardIdOrderByIdAsc(Integer cardId);

    List<GroupMember> findByDigitalCardIdAndUserId(Integer digitalCardId, Integer userId);

    void deleteByDigitalCardIdAndGroupIdIn(Integer cardId, ArrayList<Integer> groupIds);

    void deleteByPaperCardIdAndGroup_IdIn(Integer id, Set<Integer> toDelete);

    List<GroupMember> findByPaperCardId(Integer id);

    int countByGroupId(Integer groupId);
}

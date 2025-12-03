package org.example.group.service;

import org.example.group.dto.GroupMemberResponse;
import org.example.group.dto.GroupMemberUpdateRequest;
import org.example.oauth.entity.User;

import java.util.List;

public interface GroupMemberService {
    List<GroupMemberResponse> getGroupMembers(User user, Integer groupId);

    void updateGroupMembers(User user, Integer groupId, List<GroupMemberUpdateRequest.CardRef> newCards);
}

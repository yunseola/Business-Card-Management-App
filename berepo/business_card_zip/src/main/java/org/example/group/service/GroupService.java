package org.example.group.service;

import org.example.group.dto.GroupResponse;
import org.example.group.entity.Group;

import java.util.List;

public interface GroupService {

    Group createGroup(Integer userId, String groupName);

    List<GroupResponse> getMyGroups(Integer userId);

    Group updateGroup(Integer userId, Integer groupId, String groupName);

    void deleteGroup(Integer userId, Integer groupId);
}

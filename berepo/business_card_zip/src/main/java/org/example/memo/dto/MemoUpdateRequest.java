package org.example.memo.dto;

import lombok.Getter;

@Getter
public class MemoUpdateRequest {
    private String relationship;
    private String personality;
    private String workStyle;
    private String meetingNotes;
    private String etc;
    private String summary;
}

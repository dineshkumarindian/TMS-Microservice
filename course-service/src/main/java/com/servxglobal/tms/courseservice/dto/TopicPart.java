package com.servxglobal.tms.courseservice.dto;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class TopicPart {
    private Long id;
    private String title;
    private String part;
    private int session;
    private String category;
}

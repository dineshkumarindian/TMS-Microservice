package com.servxglobal.tms.courseservice.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class StreamCourse {

    private Long id;
    private Object course_logo;
    private String course_name;
    private Object level;
    private int level_id;
    private int selectedLevel;

}

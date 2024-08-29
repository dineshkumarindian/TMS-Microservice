package com.servxglobal.tms.courseservice.dto;

import com.servxglobal.tms.courseservice.model.Topic;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;

import java.util.Date;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class PartWithoutFiles {
    @Id
    private Long id;

    private int part;

    private boolean is_deleted = false;

    private Date create_time;

    private Date modified_time;

    private Long session_id;

    private int session_no;

    private Topic topicDetails;

    private Long course_id;
    
    private int max_level;

    private String filename;
}

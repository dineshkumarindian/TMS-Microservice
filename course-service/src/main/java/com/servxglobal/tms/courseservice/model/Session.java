package com.servxglobal.tms.courseservice.model;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;

@Document(collection="Sessions")
@Getter
@Setter
@ToString
public class Session {

    @Id
    private Long id;

    private int session_no;

    private boolean is_deleted = false;

    private Date created_time;

    private Date modified_time;

    private Long course_id;

    private int max_level;
}

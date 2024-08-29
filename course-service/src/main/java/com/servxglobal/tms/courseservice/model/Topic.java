package com.servxglobal.tms.courseservice.model;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;

@Document(collection="topics")
@Getter
@Setter
@ToString
public class Topic {

    @Id
    private Long id;

    private String topic_name;

    private String category;

    private boolean is_deleted = false;

    private Date created_time;

    private Date modified_time;

    private Long course_id;

}

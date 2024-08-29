package com.servxglobal.tms.batchservice.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;

@Document(collection="schedules")
@Getter
@Setter
@ToString
//@AllArgsConstructor
//@NoArgsConstructor
public class Schedule {

    @Id
    private Long id;

    private Long batch_id;

    private String batch_name;

    private String presenter;

    private String co_presenter;

    private Date schedule_date;

    private String schedule_start_time;

    private String schedule_end_time;

    private Date created_time;

    private Date modified_time;

    private String meeting_url;

    private String course_name;

    private Long course_id;

    private boolean is_deleted = false;

    private Object[] schedule_topic_details;
}

package com.servxglobal.tms.courseservice.model;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;

@Document(collection = "parts")
@Getter
@Setter
@ToString
public class Parts {

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

    private byte[] fileData;

}

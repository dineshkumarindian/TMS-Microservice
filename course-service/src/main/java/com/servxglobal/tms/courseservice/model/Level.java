package com.servxglobal.tms.courseservice.model;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.Date;

@Document(collection="levels")
@Getter
@Setter
@ToString
public class Level {

    @Id
    @Field("level_id")
    private Long id;

    private int level;

    private boolean is_deleted = false;

    private Date created_time;

    private Date modified_time;

    private Long course_id;

}

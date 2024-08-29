package com.servxglobal.tms.adminservice.model;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;

@Document(collection = "skill")
@Getter
@Setter
@ToString
public class Skill {

    @Id
    private Long id;

    private String skill;

    private boolean is_deleted = false;

    private Date created_time;

    private Date modified_time;

    private Admin createdBy;
}

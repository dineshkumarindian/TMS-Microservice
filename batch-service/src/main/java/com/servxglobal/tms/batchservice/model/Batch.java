package com.servxglobal.tms.batchservice.model;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.Date;
import java.util.List;

@Getter
@Setter
@Document(collection="batches")
@ToString
public class Batch {

    @Id
    private Long id;

    public String batch_name;

    private Date start_date;

    private Date end_date;

    private OwnerDetails ownerDetails;

    private CourseCategoryDetails courseCategoryDetails;

    private List<Long> trainersIds;

    private List<Long> traineesIds;

    private String batch_status;

    private boolean is_deleted = false;

    private boolean is_completed = false;

    private Date created_time;

    private Date modified_time;

}

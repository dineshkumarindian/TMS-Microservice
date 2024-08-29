package com.servxglobal.tms.adminservice.model;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.util.Date;

@Document(collection="branch")
@Getter
@Setter
@ToString
public class Branch {
    @Id
    private Long id;

    private String branchname;
    //branchcode should be only capital letters
    private String branch_code;

    private boolean is_deleted = false;

    private Date created_time;

    private Date modified_time;

    private Admin createdBy;
}


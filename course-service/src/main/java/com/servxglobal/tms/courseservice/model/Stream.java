package com.servxglobal.tms.courseservice.model;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.bson.types.Binary;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;

@Document(collection = "streams")
@Getter
@Setter
@ToString
public class Stream {
    @Id
    private Long id;

    private String stream_name;

    private Binary logo_Img;

    private boolean is_deleted = false;

    private Date created_time;

    private Date modified_time;

    private Object courseDetailsWithLevel;

}

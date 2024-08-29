package com.servxglobal.tms.traineeservice.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class TraineeBatch {
    private String batch_name;
    private Long batch_id;
    private String batch_status;
}

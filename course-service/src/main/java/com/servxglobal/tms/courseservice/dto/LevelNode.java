package com.servxglobal.tms.courseservice.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class LevelNode {
    private String item;
    private List<LevelNode> children;
    private TopicPart topic;
    private LevelNode parent;

    public LevelNode(String item, List<LevelNode> children) {
        this(item, children, null, null);
    }

    public LevelNode(String item) {
        this(item, new ArrayList<>(), null, null);
    }

}



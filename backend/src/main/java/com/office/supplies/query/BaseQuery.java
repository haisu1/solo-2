package com.office.supplies.query;

import lombok.Data;

import java.io.Serializable;

@Data
public class BaseQuery implements Serializable {

    private Long current = 1L;

    private Long size = 10L;

    private String keyword;

    public boolean hasKeyword() {
        return keyword != null && !keyword.trim().isEmpty();
    }
}

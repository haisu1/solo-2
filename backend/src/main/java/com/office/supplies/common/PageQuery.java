package com.office.supplies.common;

import lombok.Data;
import javax.validation.constraints.Min;

@Data
public class PageQuery {
    @Min(value = 1, message = "页码必须大于0")
    private Long current = 1L;

    @Min(value = 1, message = "每页条数必须大于0")
    private Long size = 10L;

    private String keyword;
}

package com.serendipity.entity;

import lombok.Data;

@Data
public class UserQueryCondition {
    private Integer offset;
    private Integer limit;
}

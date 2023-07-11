package com.terraform.cdk.tool.dto.conainerDef;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class RequiresAttribute {

    private String name;
    private Object targetId;
    private Object targetType;
    private Object value;

}
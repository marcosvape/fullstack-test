package com.terraform.cdk.tool.dto.conainerDef;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class Environment {

    private String name;
    private String value;

}
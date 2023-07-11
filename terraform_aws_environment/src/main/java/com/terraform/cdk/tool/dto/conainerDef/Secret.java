package com.terraform.cdk.tool.dto.conainerDef;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class Secret {

    private String valueFrom;
    private String name;

}
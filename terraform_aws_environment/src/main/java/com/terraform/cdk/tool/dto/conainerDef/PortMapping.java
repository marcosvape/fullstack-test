package com.terraform.cdk.tool.dto.conainerDef;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class PortMapping {

    private int containerPort;
    private int hostPort;
    private String protocol;

}
package com.terraform.cdk.tool.dto.conainerDef;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class LogConfiguration {

    private String logDriver;
    private Options options;

}
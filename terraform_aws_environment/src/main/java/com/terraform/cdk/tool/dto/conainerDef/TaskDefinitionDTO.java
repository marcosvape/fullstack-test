package com.terraform.cdk.tool.dto.conainerDef;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class TaskDefinitionDTO {

    private List containerDefinitions;

}
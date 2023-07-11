package com.terraform.cdk.tool.dto.conainerDef;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class TaskDefinition {

    private List containerDefinitions;
    private List compatibilities;
    private String cpu;
    private String executionRoleArn;
    private String family;
    private Object inferenceAccelerators;
    private Object ipcMode;
    private String memory;
    private String networkMode;
    private Object pidMode;
    private List placementConstraints;
    private Object proxyConfiguration;
    private List requiresAttributes;
    private List requiresCompatibilities;
    private int revision;
    private String status;
    private String taskDefinitionArn;
    private String taskRoleArn;
    private List volumes;

}
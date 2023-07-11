package com.terraform.cdk.tool.dto.conainerDef;


import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class ContainerDef {

    private LogConfiguration logConfiguration;
    private List portMappings;
    private List environment;
    private String image;
    private String name;
    private List secrets;
    private List volumesFrom;
    private List mountPoints;
    private int cpu;
    private boolean essential;
    private RepositoryCredentials repositoryCredentials;

}
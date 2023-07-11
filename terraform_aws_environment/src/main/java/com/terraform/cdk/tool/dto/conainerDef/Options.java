package com.terraform.cdk.tool.dto.conainerDef;

import com.google.gson.annotations.SerializedName;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class Options {

    @SerializedName(value = "awslogs-group")
    private String awslogsGroup;

    @SerializedName(value = "awslogs-region")
    private String awslogsRegion;

    @SerializedName(value = "awslogs-stream-prefix")
    private String awslogsStreamPrefix;

}

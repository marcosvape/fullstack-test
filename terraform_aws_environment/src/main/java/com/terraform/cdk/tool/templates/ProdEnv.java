package com.terraform.cdk.tool.templates;

import com.google.gson.Gson;
import com.hashicorp.cdktf.*;
import com.hashicorp.cdktf.providers.aws.AwsProvider;
import com.hashicorp.cdktf.providers.aws.cloudwatch.CloudwatchLogGroup;
import com.hashicorp.cdktf.providers.aws.ecr.EcrRepository;
import com.hashicorp.cdktf.providers.aws.ecs.EcsCluster;
import com.hashicorp.cdktf.providers.aws.ecs.EcsService;
import com.hashicorp.cdktf.providers.aws.ecs.EcsTaskDefinition;
import com.hashicorp.cdktf.providers.aws.elb.Alb;
import com.hashicorp.cdktf.providers.aws.elb.AlbListener;
import com.hashicorp.cdktf.providers.aws.elb.AlbTargetGroup;
import com.hashicorp.cdktf.providers.aws.iam.IamRole;
import com.hashicorp.cdktf.providers.aws.servicediscovery.ServiceDiscoveryPrivateDnsNamespace;
import com.hashicorp.cdktf.providers.aws.vpc.SecurityGroup;
import com.hashicorp.cdktf.providers.aws.vpc.Vpc;
import com.terraform.cdk.tool.dto.conainerDef.*;
import com.terraform.cdk.tool.util.ResourceProviderUtil;
import software.constructs.Construct;

import java.util.ArrayList;
import java.util.List;

public class ProdEnv extends TerraformStack {

    public Gson gson = new Gson();

    public static void main(String[] args) {
        final App app = new App();

        new ProdEnv(app, "stack");
        app.synth();
    }

    public ProdEnv(final Construct scope, final String id) {
        super(scope, id);

        String region = "us-east-2";

        AwsProvider.Builder.create(this, id)
                .region(region)
                .build();

        RemoteBackend remoteBack = new RemoteBackend(this, RemoteBackendProps.builder()
                .organization("NGTI")
                .workspaces(new NamedRemoteWorkspace("fullstack-test"))
                .build());

        String vpcName = "vpc-prod";

        Vpc vpc = new Vpc(this, vpcName, ResourceProviderUtil.generateVpc());

        String accId = "844909699887";

        String cloudWatchLogGroupName = "/vpc/vpc-flow";
        String cloudWatchId = "\"vpc-flow\"";

        List<String> subnets = ResourceProviderUtil.setupVpcSubnet(vpc.getId(), "us-east-2a", "us-east-2b", "us-east-2c", this);

        String iamRoleName = "ecsTaskExecutionRole";

        IamRole iamRole = ResourceProviderUtil.generateECSIamRole(iamRoleName, this);

        String ecsClusterName = "production";
        String clusterServiceDiscoveryZoneName = "api.internal";

        EcsCluster ecsCluster = new EcsCluster(this, ecsClusterName, ResourceProviderUtil.generateEcsCluster(ecsClusterName));

        ServiceDiscoveryPrivateDnsNamespace serviceDiscoveryPrivateDnsNamespace = new ServiceDiscoveryPrivateDnsNamespace(this, clusterServiceDiscoveryZoneName,
                ResourceProviderUtil.generateServiceDiscovery(clusterServiceDiscoveryZoneName, vpc.getId(), this));

        String ecrImageName = "fullstack-test";

        //IMAGE TAGS

        String ecrImageTag = ":latest";

        EcrRepository ecrRepository = new EcrRepository(this, ecrImageName, ResourceProviderUtil.generateEcrImage(ecrImageName));

        this.createEcsService(vpc.getId(), region, subnets,
                serviceDiscoveryPrivateDnsNamespace.getArn(), serviceDiscoveryPrivateDnsNamespace.getId(), ecsCluster.getArn(), iamRole.getArn(),
                ecrRepository.getRepositoryUrl() + ecrImageTag, clusterServiceDiscoveryZoneName);

    }

    private String createServiceWithLoadBalancer(String sgName, String vpcName, String cloudWatchLogGroupName, String cloudWatchId, String taskDefinitionName,
                                                 String containerDefinitions, String serviceName, String containerName, List<String> subnets, String serviceDiscoveryId, String clusterARN, String iamRoleARN,
                                                 String targetGroupName, String loadBalancerName, String taskMemory, String taskCpu,
                                                 Integer port, String healthCheckPath, String healthResponseCodes, Integer instancesRunning) {

        SecurityGroup securityGroup = new SecurityGroup(this, sgName,
                ResourceProviderUtil.generateAllOpenedSecurityGroup(vpcName, sgName));

        List<String> securityGroups = new ArrayList<String>();

        securityGroups.add(securityGroup.getId());

        AlbTargetGroup albTargetGroup = new AlbTargetGroup(this, targetGroupName, ResourceProviderUtil.generateIpAlbTargetGroup(targetGroupName, vpcName, healthCheckPath,
                port.toString(), healthResponseCodes));

        Alb alb = new Alb(this, loadBalancerName, ResourceProviderUtil.generateAlb(loadBalancerName, securityGroups, subnets));

        AlbListener albListener = new AlbListener(this, targetGroupName + " listener", ResourceProviderUtil.generateAlbListener(alb.getArn(),
                ResourceProviderUtil.generateAlbDefaultActions(albTargetGroup.getArn())));

        CloudwatchLogGroup logGroup = new CloudwatchLogGroup(this, cloudWatchId,
                ResourceProviderUtil.generateCloudWatchLogGroup(cloudWatchLogGroupName));

        EcsTaskDefinition taskDefinition = new EcsTaskDefinition(this, taskDefinitionName,
                ResourceProviderUtil.generateTaskDefinition(containerDefinitions, iamRoleARN, taskDefinitionName, taskMemory, taskCpu));

        EcsService ecsService = new EcsService(this, serviceName,
                ResourceProviderUtil.generateEcsService(serviceName, taskDefinition.getArn(),
                        ResourceProviderUtil.generateEcsNetworkConfigs(securityGroups, subnets),
                        ResourceProviderUtil.generateECSLoadBalancers(alb.getName(), containerName, albTargetGroup.getArn(), port), clusterARN,
                        ResourceProviderUtil.generateECSServiceDiscoveries(containerName, serviceDiscoveryId, port, this), instancesRunning));

        return alb.getDnsName();
    }

    private String createEcsService(String vpcName, String region, List<String> subnets, String clusterServiceDiscoveryZoneARN,
                                    String serviceDiscoveryId,
                                    String clusterARN, String iamRoleARN, String image, String clusterServiceDiscoveryName) {
        String securityGroupName = "fullstack-test-sg";
        String cloudWatchLogGroupName = "/ecs/fullstack-test-task-definition";
        String cloudWatchId = "\"fullstack-test-task-definition\"";

        String containerName = "fullstack-test-container";
        String taskDefinitionName = "fullstack-test-task-definition";
        String serviceName = "fullstack-test-service";
        String targetGroupName = "fullstack-test-tg";
        String loadBalancerName = "fullstack-test-lb";
        Integer port = 3000;

        Integer taskMemory = 512;
        Integer taskCpus = taskMemory / 2;

        LogConfiguration logConfiguration = new LogConfiguration("awslogs", new Options(cloudWatchLogGroupName, region, "ecs"));

        List<PortMapping> portMappings = new ArrayList<PortMapping>();
        PortMapping portMapping1 = new PortMapping(port, port, "tcp");
        portMappings.add(portMapping1);

        List<Environment> environments = new ArrayList<Environment>();

        List<Secret> secrets = new ArrayList<Secret>();

        List<String> emptyList = new ArrayList<String>();

        ContainerDef container = new ContainerDef(logConfiguration, portMappings, environments, image,
                containerName, secrets, emptyList, emptyList, 0, true, null);

        List<ContainerDef> containers = new ArrayList<ContainerDef>();
        containers.add(container);

        TaskDefinitionDTO taskDefinitionDTO = new TaskDefinitionDTO(containers);

        String dns = this.createServiceWithLoadBalancer(securityGroupName, vpcName, cloudWatchLogGroupName, cloudWatchId, taskDefinitionName,
                gson.toJson(containers), serviceName, containerName, subnets, serviceDiscoveryId,
                clusterARN, iamRoleARN, targetGroupName, loadBalancerName, taskMemory.toString(), taskCpus.toString(),
                port, "/", "200-299", 1);

        return "http://" + containerName + "." + clusterServiceDiscoveryName + ":" + port;
    }

}

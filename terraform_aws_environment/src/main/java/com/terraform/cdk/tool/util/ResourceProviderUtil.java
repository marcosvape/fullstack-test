package com.terraform.cdk.tool.util;

import com.hashicorp.cdktf.providers.aws.cloudwatch.CloudwatchLogGroupConfig;
import com.hashicorp.cdktf.providers.aws.ecr.*;
import com.hashicorp.cdktf.providers.aws.ecs.*;
import com.hashicorp.cdktf.providers.aws.elb.*;
import com.hashicorp.cdktf.providers.aws.iam.IamRole;
import com.hashicorp.cdktf.providers.aws.iam.IamRoleConfig;
import com.hashicorp.cdktf.providers.aws.iam.IamRolePolicyAttachment;
import com.hashicorp.cdktf.providers.aws.iam.IamRolePolicyAttachmentConfig;
import com.hashicorp.cdktf.providers.aws.route53.Route53RecordConfig;
import com.hashicorp.cdktf.providers.aws.route53.Route53ZoneConfig;
import com.hashicorp.cdktf.providers.aws.servicediscovery.*;
import com.hashicorp.cdktf.providers.aws.vpc.*;
import software.constructs.Construct;

import java.util.ArrayList;
import java.util.List;

public class ResourceProviderUtil {

    public static SecurityGroupConfig generateAllOpenedSecurityGroup(String vpcName, String securityGroupName) {
        List<SecurityGroupEgress> egressList = new ArrayList<SecurityGroupEgress>();

        List<String> cidrBlocks = new ArrayList<String>();

        cidrBlocks.add("0.0.0.0/0");

        List<String> cidrIpv6Blocks = new ArrayList<String>();

        cidrIpv6Blocks.add("::/0");

        egressList.add(SecurityGroupEgress.builder()
                .fromPort(0)
                .toPort(0)
                .protocol("-1")
                .cidrBlocks(cidrBlocks)
                .ipv6CidrBlocks(cidrIpv6Blocks)
                .build());

        List<SecurityGroupIngress> ingress = new ArrayList<SecurityGroupIngress>();

        ingress.add(SecurityGroupIngress.builder()
                .cidrBlocks(cidrBlocks)
                .fromPort(0)
                .toPort(0)
                .protocol("-1")
                .ipv6CidrBlocks(cidrIpv6Blocks)
                .build());

        SecurityGroupConfig sgConfig = SecurityGroupConfig.builder()
                .vpcId(vpcName)
                .name(securityGroupName)
                .egress(egressList)
                .ingress(ingress)
                .build();

        return sgConfig;
    }

    public static VpcConfig generateVpc() {
        VpcConfig vpcConfig = VpcConfig.builder()
                .cidrBlock("172.30.0.0/21")
                .enableDnsHostnames(true)
                .enableDnsSupport(true)
                .build();

        return vpcConfig;
    }

    public static List<String> setupVpcSubnet(String vpcId, String region1, String region2, String region3, Construct scope) {
        String resourceId = "subnetSetup-";
        SubnetConfig subnetConfig1 = SubnetConfig.builder()
                .vpcId(vpcId)
                .mapPublicIpOnLaunch(true)
                .cidrBlock("172.30.0.0/23")
                .availabilityZone(region1)
                .build();

        SubnetConfig subnetConfig2 = SubnetConfig.builder()
                .vpcId(vpcId)
                .mapPublicIpOnLaunch(true)
                .cidrBlock("172.30.2.0/23")
                .availabilityZone(region2)
                .build();

        Subnet subnet1 = new Subnet(scope, resourceId + "subnet1", subnetConfig1);
        Subnet subnet2 = new Subnet(scope, resourceId + "subnet2", subnetConfig2);

        InternetGatewayConfig internetGatewayConfig = InternetGatewayConfig.builder()
                .vpcId(vpcId)
                .build();

        InternetGateway gateway = new InternetGateway(scope, resourceId + "gateway", internetGatewayConfig);

        List<RouteTableRoute> routes = new ArrayList<RouteTableRoute>();

        routes.add(RouteTableRoute.builder()
                .cidrBlock("0.0.0.0/0")
                .gatewayId(gateway.getId())
                .carrierGatewayId("")
                .destinationPrefixListId("")
                .build());

        RouteTableConfig routeTableConfig = RouteTableConfig.builder()
                .route(routes)
                .vpcId(vpcId)
                .build();

        RouteTable routeTable = new RouteTable(scope, resourceId + "routeTable", routeTableConfig);

        RouteTableAssociationConfig routeTableAssociationConfig1 = RouteTableAssociationConfig.builder()
                .subnetId(subnet1.getId())
                .routeTableId(routeTable.getId())
                .build();

        RouteTableAssociationConfig routeTableAssociationConfig2 = RouteTableAssociationConfig.builder()
                .subnetId(subnet2.getId())
                .routeTableId(routeTable.getId())
                .build();

        RouteTableAssociation routeTableAssociation1 = new RouteTableAssociation(scope, resourceId + "routeTableAss1", routeTableAssociationConfig1);
        RouteTableAssociation routeTableAssociation2 = new RouteTableAssociation(scope, resourceId + "routeTableAss2", routeTableAssociationConfig2);

        List<String> subnetIds = new ArrayList<String>();

        subnetIds.add(subnet1.getId());
        subnetIds.add(subnet2.getId());

        return subnetIds;
    }

    public static IamRole generateECSIamRole(String roleName, Construct scope) {
        IamRoleConfig roleConfig = IamRoleConfig.builder()
                .assumeRolePolicy("{\n" +
                        "  \"Version\": \"2012-10-17\",\n" +
                        "  \"Statement\": [\n" +
                        "    {\n" +
                        "      \"Sid\": \"\",\n" +
                        "      \"Effect\": \"Allow\",\n" +
                        "      \"Principal\": {\n" +
                        "        \"Service\": [\n" +
                        "          \"ecs-tasks.amazonaws.com\"\n" +
                        "        ]\n" +
                        "      },\n" +
                        "      \"Action\": \"sts:AssumeRole\"\n" +
                        "    }\n" +
                        "  ]\n" +
                        "}")
                .name(roleName)
                .build();

        IamRole iamRole = new IamRole(scope, roleName, roleConfig);

        IamRolePolicyAttachmentConfig policyConfig = IamRolePolicyAttachmentConfig.builder()
                .role(roleName)
                .policyArn("arn:aws:iam::aws:policy/AdministratorAccess")
                .build();

        IamRolePolicyAttachment attachment = new IamRolePolicyAttachment(scope, roleName + "Policy", policyConfig);

        return iamRole;
    }

    public static EcsClusterConfig generateEcsCluster(String clusterName) {
        EcsClusterConfig clusterConfig = EcsClusterConfig.builder()
                .name(clusterName)
                .build();

        return clusterConfig;
    }

    public static CloudwatchLogGroupConfig generateCloudWatchLogGroup(String cloudWatchLogGroupName) {
        CloudwatchLogGroupConfig logGroupConfig = CloudwatchLogGroupConfig.builder()
                .name(cloudWatchLogGroupName)
                .retentionInDays(7)
                .build();

        return logGroupConfig;
    }

    public static EcsTaskDefinitionConfig generateTaskDefinition(String containerDefinitions, String roleARN, String taskDefinitionName, String memory, String cpu) {

        List<String> compatibilities = new ArrayList<String>();

        compatibilities.add("FARGATE");

        EcsTaskDefinitionConfig taskDefinitionConfig = EcsTaskDefinitionConfig.builder()
                .requiresCompatibilities(compatibilities)
                .taskRoleArn(roleARN)
                .executionRoleArn(roleARN)
                .memory(memory)
                .family(taskDefinitionName)
                .networkMode("awsvpc")
                .cpu(cpu)
                .containerDefinitions(containerDefinitions)
                .build();

        return taskDefinitionConfig;
    }

    public static EcsServiceConfig generateEcsService(String ecsServiceName, String taskDefinitionARN,
                                                      List<EcsServiceNetworkConfiguration> configs,
                                                      List<EcsServiceLoadBalancer> ecsLoadBalancers,
                                                      String clusterARN, List<EcsServiceServiceRegistries> serviceDiscoveries, Integer instancesRunning) {

        EcsServiceConfig ecsServiceConfig = EcsServiceConfig.builder()
                .name(ecsServiceName)
                .taskDefinition(taskDefinitionARN)
                .networkConfiguration(configs.get(0))
                .launchType("FARGATE")
                .loadBalancer(ecsLoadBalancers)
                .serviceRegistries(serviceDiscoveries.get(0))
                .desiredCount(instancesRunning)
                .cluster(clusterARN)
                .build();

        return ecsServiceConfig;
    }

    public static List<EcsServiceNetworkConfiguration> generateEcsNetworkConfigs(List<String> securityGroups, List<String> subnets) {

        List<EcsServiceNetworkConfiguration> configs = new ArrayList<EcsServiceNetworkConfiguration>();

        configs.add(EcsServiceNetworkConfiguration.builder().assignPublicIp(true).securityGroups(securityGroups).subnets(subnets).build());

        return configs;
    }

    public static AlbTargetGroupConfig generateIpAlbTargetGroup(String targetGroupName, String vpcName, String healthCheckPath, String port, String responseCodes) {

        List<AlbTargetGroupHealthCheck> healthChecks = new ArrayList<AlbTargetGroupHealthCheck>();

        AlbTargetGroupConfig albTargetGroupConfig = AlbTargetGroupConfig.builder()
                .name(targetGroupName)
                .port(Integer.valueOf(port))
                .protocol("HTTP")
                .targetType("ip")
                .slowStart(60)
                .vpcId(vpcName).slowStart(300).deregistrationDelay("300")
                .healthCheck(AlbTargetGroupHealthCheck.builder()
                        .protocol("HTTP")
                        .path(healthCheckPath)
                        .matcher(responseCodes)
                        .healthyThreshold(3)
                        .unhealthyThreshold(5)
                        .interval(300)
                        .timeout(120)
                        .port(port)
                        .build())
                .build();

        return albTargetGroupConfig;
    }

    public static AlbConfig generateAlb(String loadBalancerName, List<String> securityGroups, List<String> subnets) {

        AlbConfig albConfig = AlbConfig.builder()
                .loadBalancerType("application")
                .subnets(subnets)
                .securityGroups(securityGroups)
                .name(loadBalancerName)
                .internal(false)
                .build();

        return albConfig;
    }

    public static AlbListenerConfig generateAlbListener(String albARN, List<AlbListenerDefaultAction> defaultActions) {

        AlbListenerConfig albListenerConfig = AlbListenerConfig.builder()
                .loadBalancerArn(albARN)
                .port(80)
                .protocol("HTTP")
                .defaultAction(defaultActions)
                .build();

        return albListenerConfig;
    }

    public static List<AlbListenerDefaultAction> generateAlbDefaultActions(String albTargetGroupARN) {

        List<AlbListenerDefaultAction> defaultActions = new ArrayList<AlbListenerDefaultAction>();

        defaultActions.add(AlbListenerDefaultAction.builder()
                .type("forward")
                .targetGroupArn(albTargetGroupARN)
                .build());

        return defaultActions;
    }

    public static List<EcsServiceLoadBalancer> generateECSLoadBalancers(String albName, String containerName, String albTargetGroupARN, Integer port) {

        List<EcsServiceLoadBalancer> ecsLoadBalancers = new ArrayList<EcsServiceLoadBalancer>();

        ecsLoadBalancers.add(EcsServiceLoadBalancer.builder()
//                .elbName(albName)
                .containerName(containerName)
                .containerPort(port)
                .targetGroupArn(albTargetGroupARN)
                .build());

        return ecsLoadBalancers;
    }

    public static EcrRepositoryConfig generateEcrImage(String imageName) {
        EcrRepositoryConfig ecrRepositoryConfig = EcrRepositoryConfig.builder()
                .name(imageName)
                .build();

        return ecrRepositoryConfig;
    }

    public static List<EcsServiceServiceRegistries> generateECSServiceDiscoveries(String containerName, String namespaceId, Integer port, Construct scope) {

        ServiceDiscoveryServiceDnsConfigDnsRecords dnsConfigDnsRecords = ServiceDiscoveryServiceDnsConfigDnsRecords.builder()
                .ttl(10)
                .type("A")
                .build();

        List<ServiceDiscoveryServiceDnsConfigDnsRecords> records = new ArrayList<ServiceDiscoveryServiceDnsConfigDnsRecords>();

        records.add(dnsConfigDnsRecords);

        ServiceDiscoveryServiceDnsConfig dnsConfig = ServiceDiscoveryServiceDnsConfig.builder()
                .routingPolicy("MULTIVALUE")
                .dnsRecords(records)
                .namespaceId(namespaceId)
                .build();

        List<ServiceDiscoveryServiceDnsConfig> configs = new ArrayList<ServiceDiscoveryServiceDnsConfig>();

        configs.add(dnsConfig);

        ServiceDiscoveryServiceConfig serviceDiscoveryServiceConfig = ServiceDiscoveryServiceConfig.builder()
                .name(containerName)
                .namespaceId(namespaceId)
                .dnsConfig(dnsConfig)
                .build();

        ServiceDiscoveryService serviceDiscoveryService = new ServiceDiscoveryService(scope, containerName + "serviceDiscovery", serviceDiscoveryServiceConfig);

        List<EcsServiceServiceRegistries> serviceDiscoveries = new ArrayList<EcsServiceServiceRegistries>();

        serviceDiscoveries.add(EcsServiceServiceRegistries.builder()
                .containerName(containerName)
                .registryArn(serviceDiscoveryService.getArn())
                .build());

        return serviceDiscoveries;
    }

    public static ServiceDiscoveryPrivateDnsNamespaceConfig generateServiceDiscovery(String nameSpaceName, String vpcId, Construct scope) {

        ServiceDiscoveryPrivateDnsNamespaceConfig serviceDiscoveryPrivateDnsNamespaceConfig = ServiceDiscoveryPrivateDnsNamespaceConfig.builder()
                .description(nameSpaceName)
                .name(nameSpaceName)
                .vpc(vpcId)
                .build();

        return serviceDiscoveryPrivateDnsNamespaceConfig;
    }

}
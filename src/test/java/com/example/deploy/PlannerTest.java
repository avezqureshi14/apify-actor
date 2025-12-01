package com.example.deploy;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PlannerTest {
    @Test
    void dryRunDoesNotMentionLiveApply() {
        Manifest manifest = new Manifest("billing", "billing", "prod", "billing", 256, 512, 2);
        DeployPlan plan = new Planner().plan(manifest);
        assertTrue(plan.dryRun());
        assertTrue(plan.commands().stream().anyMatch(line -> line.contains("dry-run ecr push")));
        assertTrue(plan.commands().stream().noneMatch(line -> line.startsWith("aws ")));
    }

    @Test
    void blankServiceIsRejected() {
        Manifest manifest = new Manifest("  ", "billing", "prod", "billing", 256, 512, 1);
        assertThrows(IllegalArgumentException.class, () -> new Planner().check(manifest));
    }


    @Test
    void negativeDesiredIsRejected() {
        Manifest manifest = new Manifest("billing", "billing", "prod", "billing", 256, 512, -1);
        assertThrows(IllegalArgumentException.class, () -> new Planner().check(manifest));
    }


    @Test
    void 1024_cpu_is_a_fargate_size() {
        assertTrue(Planner.fargateCpu(1024));
    }


    @Test
    void 300_cpu_is_not_a_fargate_size() {
        assertFalse(Planner.fargateCpu(300));
    }


    @Test
    void 256_cpu_cannot_take_4096_memory() {
        assertFalse(Planner.memoryFits(256, 4096));
    }


    @Test
    void 512_cpu_can_take_1024_memory() {
        assertTrue(Planner.memoryFits(512, 1024));
    }


    @Test
    void an_image_with_a_space_is_rejected() {
        assertFalse(Planner.plainImage("my image"));
    }


    @Test
    void an_https_image_url_is_rejected() {
        assertFalse(Planner.plainImage("https://example.com/a"));
    }


    @Test
    void a_normal_image_name_passes() {
        assertTrue(Planner.plainImage("billing"));
    }


    @Test
    void zero_desired_count_is_allowed_for_a_scal() {
        Manifest m = new Manifest("billing", "billing", "prod", "billing", 256, 512, 0);
        new Planner().check(m);
    }


    @Test
    void missing_cluster_fails() {
        Manifest m = new Manifest("billing", "billing", "", "billing", 256, 512, 1);
        assertThrows(IllegalArgumentException.class, () -> new Planner().check(m));
    }


    @Test
    void missing_container_fails() {
        Manifest m = new Manifest("billing", "billing", "prod", "", 256, 512, 1);
        assertThrows(IllegalArgumentException.class, () -> new Planner().check(m));
    }


    @Test
    void 4096_cpu_is_accepted() {
        assertTrue(Planner.fargateCpu(4096));
    }


    @Test
    void 8192_cpu_is_not_on_the_small_list() {
        assertFalse(Planner.fargateCpu(8192));
    }


    @Test
    void memory_under_512_is_too_small() {
        assertFalse(Planner.memoryFits(512, 256));
    }


    @Test
    void plan_keeps_the_service_name() {
        DeployPlan plan = new Planner().plan(new Manifest("billing", "billing", "prod", "billing", 256, 512, 1));
        assertEquals("billing", plan.service());
    }


    @Test
    void docker_build_is_the_first_command() {
        DeployPlan plan = new Planner().plan(new Manifest("billing", "billing", "prod", "billing", 256, 512, 1));
        assertTrue(plan.commands().get(0).startsWith("docker build"));
    }


    @Test
    void there_are_four_commands_in_the_basic_pla() {
        DeployPlan plan = new Planner().plan(new Manifest("billing", "billing", "prod", "billing", 256, 512, 1));
        assertEquals(4, plan.commands().size());
    }


    @Test
    void usEast1() {
        assertTrue(Planner.regionOk("us-east-1"));
    }


    @Test
    void bareEastIsNotARegion() {
        assertFalse(Planner.regionOk("east-1"));
    }


    @Test
    void twelveDigitAccount() {
        assertTrue(Planner.accountOk("123456789012"));
    }


    @Test
    void shortAccount() {
        assertFalse(Planner.accountOk("123"));
    }


    @Test
    void dockerfileStaysRelative() {
        assertTrue(Planner.dockerfileOk("Dockerfile"));
        assertFalse(Planner.dockerfileOk("../Dockerfile"));
    }


    @Test
    void defaultPlatform() {
        assertEquals("linux/amd64", Planner.platformOrDefault(""));
    }


    @Test
    void keepArmPlatform() {
        assertEquals("linux/arm64", Planner.platformOrDefault("linux/arm64"));
    }


    @Test
    void graceCap() {
        assertTrue(Planner.graceOk(300));
        assertFalse(Planner.graceOk(301));
    }


    @Test
    void negativeGrace() {
        assertFalse(Planner.graceOk(-1));
    }


    @Test
    void logGroupSpaces() {
        assertFalse(Planner.logGroupOk("my group"));
        assertTrue(Planner.logGroupOk("/ecs/billing"));
    }


    @Test
    void roleArn() {
        assertTrue(Planner.roleOk("arn:aws:iam::123456789012:role/ecsTask"));
    }


    @Test
    void bareRoleName() {
        assertFalse(Planner.roleOk("ecsTask"));
    }


    @Test
    void emptyRoleIsFine() {
        assertTrue(Planner.roleOk(""));
    }


    @Test
    void publicIpWords() {
        assertTrue(Planner.publicIpWord("ENABLED"));
        assertFalse(Planner.publicIpWord("yes"));
    }


    @Test
    void minHealthy() {
        assertTrue(Planner.percentOk(50));
        assertFalse(Planner.percentOk(140));
    }


    @Test
    void maxPercentCeiling() {
        assertTrue(Planner.maxPercentOk(200));
        assertFalse(Planner.maxPercentOk(99));
    }


    @Test
    void latestTag() {
        assertTrue(Planner.tagOk("latest"));
    }


    @Test
    void tagWithSpace() {
        assertFalse(Planner.tagOk("my tag"));
    }


    @Test
    void missingTagBecomesPlan() {
        assertEquals("plan", Planner.tagOrPlan(null));
    }


    @Test
    void registryPrefix() {
        assertEquals("123.dkr.ecr.us-east-1.amazonaws.com/billing",
                Planner.registryPrefix("123.dkr.ecr.us-east-1.amazonaws.com", "billing"));
    }


    @Test
    void noRegistry() {
        assertEquals("billing", Planner.registryPrefix("", "billing"));
    }


    @Test
    void rollbackNamesService() {
        assertTrue(Planner.rollbackNote("billing").contains("billing"));
        assertTrue(Planner.rollbackNote("billing").contains("dry-run"));
    }


    @Test
    void breakerIsAnEcho() {
        assertTrue(Planner.breakerLine(true).startsWith("echo dry-run"));
    }


    @Test
    void noSubnetOverride() {
        assertTrue(Planner.subnetNote(null).contains("no subnet"));
    }


    @Test
    void subnetEcho() {
        assertTrue(Planner.subnetNote("subnet-a,subnet-b").contains("subnet-a"));
    }


    @Test
    void noSecurityGroupOverride() {
        assertTrue(Planner.sgNote("").contains("no security group"));
    }


    @Test
    void freezeCopies() {
        var frozen = Planner.freeze(java.util.List.of("echo dry-run"));
        assertEquals(1, frozen.size());
        assertThrows(UnsupportedOperationException.class, () -> frozen.add("nope"));
    }


    @Test
    void serviceSlash() {
        assertFalse(Planner.serviceNameOk("prod/billing"));
        assertTrue(Planner.serviceNameOk("billing"));
    }


    @Test
    void longCluster() {
        assertFalse(Planner.clusterNameOk("c".repeat(80)));
        assertTrue(Planner.clusterNameOk("prod"));
    }


    @Test
    void blankRegionLeavesLine() {
        assertEquals("echo dry-run", Planner.withRegion("echo dry-run", " "));
    }


    @Test
    void regionFlag() {
        assertTrue(Planner.withRegion("echo dry-run", "us-east-1").endsWith("--region us-east-1"));
    }


    @Test
    void singleTask() {
        new Planner().check(new Manifest("billing", "billing", "prod", "billing", 512, 1024, 1));
    }


    @Test
    void cpu2048() {
        assertTrue(Planner.fargateCpu(2048));
    }


    @Test
    void zeroMemory() {
        assertFalse(Planner.memoryFits(512, 0));
    }


    @Test
    void tagLineUsesImage() {
        DeployPlan plan = new Planner().plan(new Manifest("api", "api", "dev", "api", 256, 512, 1));
        assertTrue(plan.commands().get(1).contains("api:latest"));
    }


    @Test
    void ecsLineHasCluster() {
        DeployPlan plan = new Planner().plan(new Manifest("api", "api", "dev", "api", 256, 512, 1));
        assertTrue(plan.commands().get(3).contains("--cluster dev"));
    }


    @Test
    void fourLinesForApi() {
        assertEquals(4, new Planner().plan(new Manifest("api", "api", "dev", "api", 256, 512, 1)).commands().size());
    }


    @Test
    void halfCpuTwoGig() {
        assertTrue(Planner.memoryFits(512, 2048));
    }

}

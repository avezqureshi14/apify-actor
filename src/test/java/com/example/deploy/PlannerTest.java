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

}

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

}

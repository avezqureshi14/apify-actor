package com.example.deploy;

import java.util.ArrayList;
import java.util.List;


public class Planner {
    public DeployPlan plan(Manifest manifest) {
        List<String> commands = new ArrayList<>();
        commands.add("docker build -t " + manifest.image() + ":plan .");
        commands.add("docker tag " + manifest.image() + ":plan " + manifest.image() + ":latest");
        commands.add("echo dry-run ecr push " + manifest.image() + ":latest");
        commands.add("echo dry-run ecs update-service --cluster " + manifest.cluster()
                + " --service " + manifest.service()
                + " --desired-count " + manifest.desired());
        return new DeployPlan(manifest.service(), true, List.copyOf(commands));
    }

    public void check(Manifest manifest) {
        if (manifest.service() == null || manifest.service().isBlank()) {
            throw new IllegalArgumentException("service name is required");
        }
        if (manifest.cluster() == null || manifest.cluster().isBlank()) {
            throw new IllegalArgumentException("cluster is required");
        }
        if (manifest.desired() < 0) {
            throw new IllegalArgumentException("desired count cannot be negative");
        }
    }

}

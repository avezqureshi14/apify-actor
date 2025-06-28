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
        return new DeployPlan(manifest.service(), true, commands);
    }
}

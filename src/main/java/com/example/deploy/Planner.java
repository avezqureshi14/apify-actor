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
                + " --container " + manifest.container()
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
        if (!fargateCpu(manifest.cpu())) {
            throw new IllegalArgumentException("cpu is not a fargate size");
        }
        if (!memoryFits(manifest.cpu(), manifest.memory())) {
            throw new IllegalArgumentException("memory does not fit the cpu");
        }
        if (!plainImage(manifest.image())) {
            throw new IllegalArgumentException("image should be a name, not a url");
        }
        if (manifest.container() == null || manifest.container().isBlank()) {
            throw new IllegalArgumentException("container name is required");
        }
    }


    static boolean fargateCpu(int cpu) {
        return cpu == 256 || cpu == 512 || cpu == 1024 || cpu == 2048 || cpu == 4096;
    }


    static boolean memoryFits(int cpu, int memory) {
        if (memory < 512 || memory > 30720) {
            return false;
        }
        if (cpu == 256) {
            return memory <= 2048;
        }
        return true;
    }


    static boolean plainImage(String image) {
        return image != null && !image.isBlank() && !image.contains(" ") && !image.contains("://");
    }


    static boolean regionOk(String region) {
        return region != null && region.matches("[a-z]{2}-[a-z]+-\\d");
    }


    static boolean accountOk(String account) {
        return account != null && account.matches("\\d{12}");
    }


    static boolean dockerfileOk(String path) {
        return path != null && !path.startsWith("/") && !path.contains("..");
    }


    static String platformOrDefault(String platform) {
        if (platform == null || platform.isBlank()) {
            return "linux/amd64";
        }
        return platform;
    }


    static boolean graceOk(int seconds) {
        return seconds >= 0 && seconds <= 300;
    }


    static boolean logGroupOk(String name) {
        return name != null && !name.isBlank() && !name.contains(" ");
    }

}

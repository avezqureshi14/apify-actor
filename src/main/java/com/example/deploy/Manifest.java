package com.example.deploy;

public record Manifest(
        String service,
        String image,
        String cluster,
        String container,
        int cpu,
        int memory,
        int desired
) {}

package com.example.deploy;

import java.util.List;

public record DeployPlan(String service, boolean dryRun, List<String> commands) {}

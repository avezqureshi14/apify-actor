package com.example.deploy;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.nio.file.Path;

@RestController
public class PlanController {
    private final Planner planner = new Planner();
    private final ObjectMapper mapper = new ObjectMapper();

    @PostMapping("/v1/plan")
    public DeployPlan plan(@RequestBody Manifest manifest) {
        return planner.plan(manifest);
    }

    public Manifest readManifest(Path path) throws Exception {
        return mapper.readValue(path.toFile(), Manifest.class);
    }
}

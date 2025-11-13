# aws-deploy-pipeline

Reads a local manifest and prints a dry-run deploy plan: docker build, an ECR push we do not perform, and an ECS service update we do not perform.

No credentials are read. `dryRun` on the response stays true.

Try `manifest.sample.json` against POST /v1/plan.

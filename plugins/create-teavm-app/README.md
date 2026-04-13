# create-teavm-app Plugin

Claude Code plugin that provides AI assistant guidance for scaffolding full-stack **TeaVM + Cloud Run + Firebase** applications using the `create-teavm-app` CLI.

## Installation

```bash
# Add the marketplace
/plugin marketplace add webliteca/create-teavm-app-cli

# Install the plugin
/plugin install create-teavm-app@create-teavm-app-marketplace
```

## What This Plugin Does

Once installed, Claude Code will automatically know how to:

- **Scaffold new projects** using `npx create-teavm-app` with the correct parameters
- **Understand generated project structure** — modules, POMs, Docker configs, workflows
- **Write correct teavm-lambda code** — Main.java, Resources, Frontend App.java with proper imports and patterns
- **Extend generated projects** — add new API resources, frontend pages, processor modules
- **Avoid common mistakes** — no reflection, immutable Response, correct annotation packages

## When Claude Uses This Skill

The skill activates when you ask Claude to:
- Create or scaffold a new TeaVM application
- Start a new Java Cloud Run / Firebase project
- Generate a project using teavm-lambda or teavm-react
- Set up a full-stack Java serverless application

## Example Prompts

```
"Create a new TeaVM app called photo-gallery with database and auth"
"Scaffold a minimal API-only service with no frontend"
"Generate a project with an image-processor service"
"Set up a new teavm-lambda project for a REST API"
```

## CLI Quick Reference

```bash
# Full-featured app (defaults: database + auth + frontend + workflows)
npx create-teavm-app --name my-app --package ca.weblite.myapp

# Minimal API-only
npx create-teavm-app --name my-api --package ca.weblite.myapi \
  --no-database --no-auth --skip-frontend --skip-workflows

# With object storage and a processor
npx create-teavm-app --name media-app --package ca.weblite.mediaapp \
  --object-store --processor image-processor

# Custom description and group ID
npx create-teavm-app --name my-app --package com.example.myapp \
  --group-id com.example --description "My awesome app"
```

## Learn More

- [create-teavm-app CLI Documentation](../../README.md)
- [teavm-lambda framework](https://github.com/webliteca/teavm-lambda)
- [teavm-react library](https://github.com/webliteca/teavm-react)

# Apache Camel Upgrade Recipes - AI Agent Guidelines

Guidelines for AI agents working on this codebase.

## Project Info

Apache Camel Upgrade Recipes is a collection of migration recipes that assist in upgrading Camel and Camel Spring Boot projects between versions

## AI Agent Rules of Engagement

These rules apply to ALL AI agents working on this codebase.

### Attribution

- All AI-generated content (GitHub PR descriptions, review comments, JIRA comments) MUST clearly
  identify itself as AI-generated and mention the human operator.
  Example: "_Claude Code on behalf of [Human Name]_"

### PR Volume

- An agent MUST NOT open more than 10 PRs per day per operator to ensure human reviewers can keep up.
- Prioritize quality over quantity — fewer well-tested PRs are better than many shallow ones.

### Branch Ownership

- An agent MUST NEVER push commits to a branch it did not create.
- If a contributor's PR needs changes, the agent may suggest changes via review comments,
  but must not push to their branch without explicit permission.

### JIRA Ticket Ownership

- An agent MUST ONLY pick up **Unassigned** JIRA tickets.
- If a ticket is already assigned to a human, the agent must not reassign it or work on it.
- Before starting work, the agent must assign the ticket to its operator and transition it to "In Progress".
- Before closing a ticket, always set the correct `fixVersions` field.
  Note: `fixVersions` cannot be set on an already-closed issue — set it before closing,
  or reopen/set/close if needed.

### Merge Requirements

- An agent MUST NOT merge a PR if there are any **unresolved review conversations**.
- An agent MUST NOT merge a PR without at least **one human approval**.
- An agent MUST NOT approve its own PRs — human review is always required.

### License Constraints

- Recipes added must be licensed under the Apache License
- Any recipes added in the recipeList of a Recipe must be licensed under the Apache License
- Do not use recipes released under the Moderne Proprietary License or the Moderne Source Available License in the recipeList of a recipe
- Check the licensing of a recipe before adding it to a recipeList

### Code Quality

- Every PR must include tests for new functionality or bug fixes.
- Every PR must include documentation updates where applicable.
- All generated files must be regenerated and committed (CI checks for uncommitted changes).

## Project Structure
```
camel-upgrade-recipes/
├── camel-upgrade-recipes/          # Core Camel migration recipes
│   └── src/main/resources/META-INF/rewrite/*.yaml  # Recipe definitions
├── camel-spring-boot-upgrade-recipes/  # Spring Boot specific recipes
├── pom.xml                         # Parent POM
└── release-utils/                  # Release automation scripts
```

## Build

```bash
mvn clean install                     # full build
```

## Testing

```bash
mvn test              # unit tests
```

## Links

- https://camel.apache.org/
- https://github.com/apache/camel-upgrade-recipes
- https://issues.apache.org/jira/browse/CAMEL
- dev@camel.apache.org
- https://camel.zulipchat.com/
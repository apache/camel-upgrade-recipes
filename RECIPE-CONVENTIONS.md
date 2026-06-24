# Recipe Organization Convention: Patch vs. Main Migration Recipes

When a breaking change is backported to an LTS patch release (e.g., 4.18.3) and also ships in the next main version (e.g., 4.21), the same migration recipe is needed in both upgrade paths. This document describes the convention the project follows to avoid duplicating Java code and YAML definitions.

## The Convention

**Rule: Implementation lives in the patch package; newer versions reference it.**

When a patch release (e.g., 4.18.3) and a main release (e.g., 4.21) both require the same migration:

1. **Java implementation** goes into the patch-version package (e.g., `org.apache.camel.upgrade.camel418_3`)
2. **YAML recipe definitions** using those Java classes are defined in the patch YAML file (e.g., `4.18.3.yaml`) with recipe names in the patch namespace (e.g., `camel418_3.upgradeJGroupsHeaders`)
3. **Newer main-version YAMLs** (e.g., `4.21.yaml`) reference the patch recipes -- they do NOT duplicate the Java classes or redefine the YAML recipes under their own namespace

### Example: Header Renames (4.18.3 / 4.21)

**Java classes** -- all in `src/main/java/org/apache/camel/upgrade/camel418_3/`:
- `RenameHeaders.java`, `RenameHeaderPrefixes.java`, and their visitor classes

**No `camel421/` Java package exists** -- 4.21 has no header rename Java code of its own.

**4.18.3.yaml** defines the recipes with their implementations:
```yaml
name: org.apache.camel.upgrade.camel418_3.upgradeJGroupsHeaders
recipeList:
  - org.apache.camel.upgrade.camel418_3.RenameHeaders:   # points to patch Java class
      headerMappings:
        JGROUPS_CHANNEL_ADDRESS: CamelJGroupsChannelAddress
```

**4.21.yaml** reuses those patch recipes in two ways:

1. Referencing whole patch-defined recipes directly:
```yaml
name: org.apache.camel.upgrade.camel421.CamelMigrationRecipe
recipeList:
  ## inherited from patch recipe
  - org.apache.camel.upgrade.camel418_3.upgradeJGroupsHeaders    # reuses 4.18.3 recipe
  - org.apache.camel.upgrade.camel418_3.upgradeJiraHeaders
  - org.apache.camel.upgrade.camel418_3.upgradeShiroHeaders
  ...
```

2. Defining new 4.21-specific YAML recipes that reuse the patch Java class:
```yaml
name: org.apache.camel.upgrade.camel421.upgradeKafkaRecipes
recipeList:
  - org.apache.camel.upgrade.camel418_3.RenameHeaders:   # reuses patch Java class
      headerMappings:
        kafka.PARTITION_KEY: CamelKafkaPartitionKey
```

### Example: Saga EIP (4.18.1 / 4.19)

**Java classes** in `camel418_1/`: `XmlDsl419SagaRecipe.java`, `YamlDsl419SagaRecipe.java`

**4.18.1.yaml** defines the saga recipe:
```yaml
name: org.apache.camel.upgrade.camel418_1.saga
recipeList:
  - org.apache.camel.upgrade.camel418_1.XmlDsl419SagaRecipe
  - org.apache.camel.upgrade.camel418_1.YamlDsl419SagaRecipe
```

**4.19.yaml** references it:
```yaml
name: org.apache.camel.upgrade.camel419.CamelMigrationRecipe
recipeList:
  - org.apache.camel.upgrade.camel418_1.saga        # reuses 4.18.1 recipe
  - org.apache.camel.upgrade.camel419.removedComponents
```

## LTS Aggregation

LTS recipes (e.g., `4.18LTS.yaml`) chain patch recipes in reverse chronological order:
```
camel418_3.CamelMigrationRecipe  (4.18.1 -> 4.18.3)
camel418_1.CamelMigrationRecipe  (4.18.0 -> 4.18.1)
camel418.CamelMigrationRecipe    (4.17   -> 4.18)
... older versions
```

## Exception: 4.10.x

The 4.10.4 patch predates this convention and does not follow it -- its implementation lives in the main-version package (`camel412`) rather than in a dedicated patch package.

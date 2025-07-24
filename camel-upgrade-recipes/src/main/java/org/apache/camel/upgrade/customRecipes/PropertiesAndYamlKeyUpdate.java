package org.apache.camel.upgrade.customRecipes;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.openrewrite.Option;
import org.openrewrite.Recipe;
import org.openrewrite.properties.ChangePropertyKey;

import java.util.List;

@EqualsAndHashCode(callSuper = false)
@RequiredArgsConstructor
@AllArgsConstructor
@Setter
public class PropertiesAndYamlKeyUpdate extends Recipe {

    @Option(example = "TODO Provide a usage example for the docs", displayName = "Old configuration key",
            description = "The configuration key to rename.")
    String oldPropertyKey;

    @Option(example = "TODO Provide a usage example for the docs", displayName = "New configuration key",
            description = "The configuration to be replaced with.")
    String newPropertyKey;

    @Override
    public String getDisplayName() {
        return "Update Apache Camel configurations keys";
    }

    @Override
    public String getDescription() {
        return "Update Apache Camel configurations keys";
    }

    @Override
    public List<Recipe> getRecipeList() {
        return List.of(new ChangePropertyKey(oldPropertyKey, newPropertyKey,
                null, null),
                new org.openrewrite.yaml.ChangePropertyKey(
                        oldPropertyKey, newPropertyKey, null, null, null));
    }
}

package org.apache.camel.upgrade;

import org.openrewrite.ExecutionContext;
import org.openrewrite.Option;
import org.openrewrite.Recipe;
import org.openrewrite.TreeVisitor;
import org.openrewrite.java.JavaIsoVisitor;
import org.openrewrite.java.marker.JavaVersion;
import org.openrewrite.java.tree.J;
import org.openrewrite.maven.UpdateMavenProjectPropertyJavaVersion;
import org.openrewrite.maven.UseMavenCompilerPluginReleaseConfiguration;

import java.time.Duration;
import java.util.*;

public class UpgradeJavaVersion extends Recipe {

    @Option(displayName = "Java version",
            description = "The Java version to upgrade to.",
            example = "17")
    Integer version;

    @Override
    public String getDisplayName() {
        return "Upgrade Java version";
    }

    @Override
    public String getDescription() {
        return "Upgrade build plugin configuration to use the specified Java version. " +
                "This recipe changes `java.toolchain.languageVersion` in `build.gradle(.kts)` of gradle projects, " +
                "or maven-compiler-plugin target version and related settings. " +
                "Will not downgrade if the version is newer than the specified version.";
    }

    @Override
    public List<Recipe> getRecipeList() {
        return Arrays.asList(
                new UseMavenCompilerPluginReleaseConfiguration(version),
                new UpdateMavenProjectPropertyJavaVersion(version)
        );
    }

    /**
     * This recipe only updates markers, so it does not correspond to human manual effort.
     *
     * @return Zero estimated time.
     */
    @Override
    public Duration getEstimatedEffortPerOccurrence() {
        return Duration.ofMinutes(0);
    }

    @Override
    public TreeVisitor<?, ExecutionContext> getVisitor() {
        String newVersion = version.toString();
        Map<JavaVersion, JavaVersion> updatedMarkers = new HashMap<>();
        return new JavaIsoVisitor<>() {
            @Override
            public J preVisit(J tree, ExecutionContext ctx) {
                Optional<JavaVersion> maybeJavaVersion = tree.getMarkers().findFirst(JavaVersion.class);
                if (maybeJavaVersion.isPresent() && maybeJavaVersion.get().getMajorVersion() < version) {
                    return tree.withMarkers(tree.getMarkers().setByType(updatedMarkers.computeIfAbsent(maybeJavaVersion.get(),
                            m -> m.withSourceCompatibility(newVersion).withTargetCompatibility(newVersion))));
                }
                return tree;
            }
        };
    }

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }
}

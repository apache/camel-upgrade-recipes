package org.apache.camel.upgrade.camel416;

import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import org.apache.camel.upgrade.AbstractCamelJavaVisitor;
import org.apache.camel.upgrade.RecipesUtil;
import org.openrewrite.ExecutionContext;
import org.openrewrite.Recipe;
import org.openrewrite.Tree;
import org.openrewrite.TreeVisitor;
import org.openrewrite.java.MethodMatcher;
import org.openrewrite.java.tree.J;
import org.openrewrite.java.tree.Space;
import org.openrewrite.java.tree.VariableDeclarator;
import org.openrewrite.marker.Markers;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * <a href="https://camel.apache.org/manual/camel-4x-upgrade-guide-4_16.html#_subscription_monitoring_api_changes">Java Milo Subscription API changes</a>
 */
@EqualsAndHashCode(callSuper = false)
@RequiredArgsConstructor
public class Camel416MiloLambdaRecipe extends Recipe {

    private static final MethodMatcher MATCHER =
            new MethodMatcher("org.eclipse.milo.opcua.sdk.client.subscriptions.OpcUaMonitoredItem setValueConsumer(java.util.function.Consumer)"); //todo use consumer

    @Override
    public String getDisplayName() {
        return "Milo: The monitored item data value listener API has changed";
    }

    @Override
    public String getDescription() {
        return "Milo: The monitored item data value listener API has changed.";
    }

    @Override
    public TreeVisitor<?, ExecutionContext> getVisitor() {
        return RecipesUtil.newVisitor(new AbstractCamelJavaVisitor() {

           /* todo better solution would be to use templates, but I'm not able to change a lambda parameter in any way
           private final JavaTemplate template = JavaTemplate.builder(
                    "#{any(org.eclipse.milo.opcua.sdk.client.subscriptions.OpcUaMonitoredItem)}.setValueConsumer(" +
                            "(item) -> #{any(org.openrewrite.java.tree.J)};)").build();
            private final JavaTemplate template2 = JavaTemplate.builder(
                    "item -> #{any(org.openrewrite.java.tree.J"
            ).build();
            */

            @Override
            public J.MethodInvocation doVisitMethodInvocation(J.MethodInvocation methodInvocation, ExecutionContext ctx) {
                J.MethodInvocation mi = super.doVisitMethodInvocation(methodInvocation, ctx);
                //in case the consumer is a lambda function
                if (MATCHER.matches(mi) && mi.getArguments().get(0) instanceof J.Lambda) {
                    //add a parameter 'item' into the consumer
                    J.Lambda l = (J.Lambda) mi.getArguments().get(0);
                    J.VariableDeclarations existingParam = (J.VariableDeclarations) l.getParameters().getParameters().get(0);

                    //new `item` identifier
                    VariableDeclarator itemIdentifier = new J.Identifier(Tree.randomId(), Space.EMPTY, Markers.EMPTY, Collections.emptyList(), "item", null, null);

                    //Named Variable is created from `item`
                    J.VariableDeclarations.NamedVariable itemNamedVariable = new J.VariableDeclarations.NamedVariable(
                            Tree.randomId(),
                            Space.EMPTY,
                            Markers.EMPTY,
                            itemIdentifier,
                            Collections.emptyList(),
                            null,
                            null
                    );

                    J.VariableDeclarations newVds = ((J.VariableDeclarations) l.getParameters().getParameters().get(0)).withVariables(Collections.singletonList(itemNamedVariable));

                    //add `item` before original parameter
                    List<J> newParameters = Arrays.asList(newVds, existingParam);
                    l = l.withParameters(l.getParameters().withParameters(newParameters).withParenthesized(true));


                    //change parameters and method name
                    mi = mi.withName(mi.getName().withSimpleName("setDataValueListener"));
                    mi = mi.withArguments(Collections.singletonList(l));
                }
                return mi;
            }
        });
    }
}

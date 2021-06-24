package org.kie.baaas.dfs.api;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.fabric8.kubernetes.api.builder.Nested;
import java.net.URI;
import javax.validation.constraints.NotNull;
import java.lang.Deprecated;
import javax.validation.Valid;
import io.fabric8.kubernetes.api.builder.BaseFluent;
import java.util.Collection;
import java.lang.Object;
import java.lang.Boolean;

public class DecisionSpecFluentImpl<A extends DecisionSpecFluent<A>> extends io.fabric8.kubernetes.api.builder.BaseFluent<A> implements DecisionSpecFluent<A> {

    private DecisionVersionSpecBuilder definition;
    private Collection<URI> webhooks;

    public DecisionSpecFluentImpl() {
    }

    public DecisionSpecFluentImpl(DecisionSpec instance) {
        this.withDefinition(instance.getDefinition());
        
        this.withWebhooks(instance.getWebhooks());
    }

    
/**
 * This method has been deprecated, please use method buildDefinition instead.
 * @return The buildable object.
 */
@Deprecated public DecisionVersionSpec getDefinition() {
        return this.definition!=null?this.definition.build():null;
    }

    public DecisionVersionSpec buildDefinition() {
        return this.definition!=null?this.definition.build():null;
    }

    public A withDefinition(DecisionVersionSpec definition) {
        _visitables.get("definition").remove(this.definition);
        if (definition!=null){ this.definition= new DecisionVersionSpecBuilder(definition); _visitables.get("definition").add(this.definition);} return (A) this;
    }

    public Boolean hasDefinition() {
        return this.definition != null;
    }

    public DecisionSpecFluent.DefinitionNested<A> withNewDefinition() {
        return new DefinitionNestedImpl();
    }

    public DecisionSpecFluent.DefinitionNested<A> withNewDefinitionLike(DecisionVersionSpec item) {
        return new DefinitionNestedImpl(item);
    }

    public DecisionSpecFluent.DefinitionNested<A> editDefinition() {
        return withNewDefinitionLike(getDefinition());
    }

    public DecisionSpecFluent.DefinitionNested<A> editOrNewDefinition() {
        return withNewDefinitionLike(getDefinition() != null ? getDefinition(): new DecisionVersionSpecBuilder().build());
    }

    public DecisionSpecFluent.DefinitionNested<A> editOrNewDefinitionLike(DecisionVersionSpec item) {
        return withNewDefinitionLike(getDefinition() != null ? getDefinition(): item);
    }

    public Collection<URI> getWebhooks() {
        return this.webhooks;
    }

    public A withWebhooks(Collection<URI> webhooks) {
        this.webhooks=webhooks; return (A) this;
    }

    public Boolean hasWebhooks() {
        return this.webhooks != null;
    }

    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DecisionSpecFluentImpl that = (DecisionSpecFluentImpl) o;
        if (definition != null ? !definition.equals(that.definition) :that.definition != null) return false;
        if (webhooks != null ? !webhooks.equals(that.webhooks) :that.webhooks != null) return false;
        return true;
    }

    public int hashCode() {
        return java.util.Objects.hash(definition,  webhooks,  super.hashCode());
    }

    public class DefinitionNestedImpl<N> extends DecisionVersionSpecFluentImpl<DecisionSpecFluent.DefinitionNested<N>> implements DecisionSpecFluent.DefinitionNested<N>,io.fabric8.kubernetes.api.builder.Nested<N> {
        private final DecisionVersionSpecBuilder builder;

            DefinitionNestedImpl(DecisionVersionSpec item) {
                this.builder = new DecisionVersionSpecBuilder(this, item);
                        
            }

            DefinitionNestedImpl() {
                this.builder = new DecisionVersionSpecBuilder(this);
                        
            }

            public N and() {
                return (N) DecisionSpecFluentImpl.this.withDefinition(builder.build());
            }

            public N endDefinition() {
                return and();
            }
    }


}

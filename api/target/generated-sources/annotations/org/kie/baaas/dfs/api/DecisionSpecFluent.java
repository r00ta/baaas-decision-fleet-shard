package org.kie.baaas.dfs.api;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.fabric8.kubernetes.api.builder.Fluent;
import io.fabric8.kubernetes.api.builder.Nested;
import java.net.URI;
import javax.validation.constraints.NotNull;
import java.lang.Deprecated;
import javax.validation.Valid;
import java.util.Collection;
import java.lang.Boolean;

public interface DecisionSpecFluent<A extends DecisionSpecFluent<A>> extends Fluent<A> {


    
/**
 * This method has been deprecated, please use method buildDefinition instead.
 * @return The buildable object.
 */
@Deprecated public DecisionVersionSpec getDefinition();
    public DecisionVersionSpec buildDefinition();
    public A withDefinition(DecisionVersionSpec definition);
    public Boolean hasDefinition();
    public DecisionSpecFluent.DefinitionNested<A> withNewDefinition();
    public DecisionSpecFluent.DefinitionNested<A> withNewDefinitionLike(DecisionVersionSpec item);
    public DecisionSpecFluent.DefinitionNested<A> editDefinition();
    public DecisionSpecFluent.DefinitionNested<A> editOrNewDefinition();
    public DecisionSpecFluent.DefinitionNested<A> editOrNewDefinitionLike(DecisionVersionSpec item);
    public Collection<URI> getWebhooks();
    public A withWebhooks(Collection<URI> webhooks);
    public Boolean hasWebhooks();
    public interface DefinitionNested<N> extends io.fabric8.kubernetes.api.builder.Nested<N>,DecisionVersionSpecFluent<DecisionSpecFluent.DefinitionNested<N>> {

            public N and();
            public N endDefinition();    }


}

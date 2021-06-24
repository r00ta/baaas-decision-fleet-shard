package org.kie.baaas.dfs.api;

import io.fabric8.kubernetes.api.builder.VisitableBuilder;
import java.lang.Object;
import java.lang.Boolean;

public class DecisionRequestBuilder extends DecisionRequestFluentImpl<DecisionRequestBuilder> implements VisitableBuilder<DecisionRequest,DecisionRequestBuilder> {

    DecisionRequestFluent<?> fluent;
    Boolean validationEnabled;

    public DecisionRequestBuilder() {
        this(true);
    }

    public DecisionRequestBuilder(Boolean validationEnabled) {
        this(new DecisionRequest(), validationEnabled);
    }

    public DecisionRequestBuilder(DecisionRequestFluent<?> fluent) {
        this(fluent, true);
    }

    public DecisionRequestBuilder(DecisionRequestFluent<?> fluent,Boolean validationEnabled) {
        this(fluent, new DecisionRequest(), validationEnabled);
    }

    public DecisionRequestBuilder(DecisionRequestFluent<?> fluent,DecisionRequest instance) {
        this(fluent, instance, true);
    }

    public DecisionRequestBuilder(DecisionRequestFluent<?> fluent,DecisionRequest instance,Boolean validationEnabled) {
        this.fluent = fluent; 
        fluent.withMetadata(instance.getMetadata());
        
        fluent.withSpec(instance.getSpec());
        
        fluent.withStatus(instance.getStatus());
        
        fluent.withKind(instance.getKind());
        
        fluent.withApiVersion(instance.getApiVersion());
        
        this.validationEnabled = validationEnabled; 
    }

    public DecisionRequestBuilder(DecisionRequest instance) {
        this(instance,true);
    }

    public DecisionRequestBuilder(DecisionRequest instance,Boolean validationEnabled) {
        this.fluent = this; 
        this.withMetadata(instance.getMetadata());
        
        this.withSpec(instance.getSpec());
        
        this.withStatus(instance.getStatus());
        
        this.withKind(instance.getKind());
        
        this.withApiVersion(instance.getApiVersion());
        
        this.validationEnabled = validationEnabled; 
    }

    public DecisionRequest build() {
        DecisionRequest buildable = new DecisionRequest();
        buildable.setMetadata(fluent.getMetadata());
        buildable.setSpec(fluent.getSpec());
        buildable.setStatus(fluent.getStatus());
        buildable.setKind(fluent.getKind());
        buildable.setApiVersion(fluent.getApiVersion());
        return buildable;
    }

    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        DecisionRequestBuilder that = (DecisionRequestBuilder) o;
        if (fluent != null &&fluent != this ? !fluent.equals(that.fluent) :that.fluent != null &&fluent != this ) return false;
        
        if (validationEnabled != null ? !validationEnabled.equals(that.validationEnabled) :that.validationEnabled != null) return false;
        return true;
    }

    public int hashCode() {
        return java.util.Objects.hash(fluent,  validationEnabled,  super.hashCode());
    }

}

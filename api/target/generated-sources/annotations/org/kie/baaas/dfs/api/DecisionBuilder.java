package org.kie.baaas.dfs.api;

import io.fabric8.kubernetes.api.builder.VisitableBuilder;
import java.lang.Object;
import java.lang.Boolean;

public class DecisionBuilder extends DecisionFluentImpl<DecisionBuilder> implements VisitableBuilder<Decision,DecisionBuilder> {

    DecisionFluent<?> fluent;
    Boolean validationEnabled;

    public DecisionBuilder() {
        this(true);
    }

    public DecisionBuilder(Boolean validationEnabled) {
        this(new Decision(), validationEnabled);
    }

    public DecisionBuilder(DecisionFluent<?> fluent) {
        this(fluent, true);
    }

    public DecisionBuilder(DecisionFluent<?> fluent,Boolean validationEnabled) {
        this(fluent, new Decision(), validationEnabled);
    }

    public DecisionBuilder(DecisionFluent<?> fluent,Decision instance) {
        this(fluent, instance, true);
    }

    public DecisionBuilder(DecisionFluent<?> fluent,Decision instance,Boolean validationEnabled) {
        this.fluent = fluent; 
        fluent.withMetadata(instance.getMetadata());
        
        fluent.withSpec(instance.getSpec());
        
        fluent.withStatus(instance.getStatus());
        
        fluent.withKind(instance.getKind());
        
        fluent.withApiVersion(instance.getApiVersion());
        
        this.validationEnabled = validationEnabled; 
    }

    public DecisionBuilder(Decision instance) {
        this(instance,true);
    }

    public DecisionBuilder(Decision instance,Boolean validationEnabled) {
        this.fluent = this; 
        this.withMetadata(instance.getMetadata());
        
        this.withSpec(instance.getSpec());
        
        this.withStatus(instance.getStatus());
        
        this.withKind(instance.getKind());
        
        this.withApiVersion(instance.getApiVersion());
        
        this.validationEnabled = validationEnabled; 
    }

    public Decision build() {
        Decision buildable = new Decision();
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
        DecisionBuilder that = (DecisionBuilder) o;
        if (fluent != null &&fluent != this ? !fluent.equals(that.fluent) :that.fluent != null &&fluent != this ) return false;
        
        if (validationEnabled != null ? !validationEnabled.equals(that.validationEnabled) :that.validationEnabled != null) return false;
        return true;
    }

    public int hashCode() {
        return java.util.Objects.hash(fluent,  validationEnabled,  super.hashCode());
    }

}

package org.kie.baaas.dfs.api;

import io.fabric8.kubernetes.api.builder.VisitableBuilder;
import java.lang.Object;
import java.lang.Boolean;

public class DecisionVersionBuilder extends DecisionVersionFluentImpl<DecisionVersionBuilder> implements VisitableBuilder<DecisionVersion,DecisionVersionBuilder> {

    DecisionVersionFluent<?> fluent;
    Boolean validationEnabled;

    public DecisionVersionBuilder() {
        this(true);
    }

    public DecisionVersionBuilder(Boolean validationEnabled) {
        this(new DecisionVersion(), validationEnabled);
    }

    public DecisionVersionBuilder(DecisionVersionFluent<?> fluent) {
        this(fluent, true);
    }

    public DecisionVersionBuilder(DecisionVersionFluent<?> fluent,Boolean validationEnabled) {
        this(fluent, new DecisionVersion(), validationEnabled);
    }

    public DecisionVersionBuilder(DecisionVersionFluent<?> fluent,DecisionVersion instance) {
        this(fluent, instance, true);
    }

    public DecisionVersionBuilder(DecisionVersionFluent<?> fluent,DecisionVersion instance,Boolean validationEnabled) {
        this.fluent = fluent; 
        fluent.withMetadata(instance.getMetadata());
        
        fluent.withSpec(instance.getSpec());
        
        fluent.withStatus(instance.getStatus());
        
        fluent.withKind(instance.getKind());
        
        fluent.withApiVersion(instance.getApiVersion());
        
        this.validationEnabled = validationEnabled; 
    }

    public DecisionVersionBuilder(DecisionVersion instance) {
        this(instance,true);
    }

    public DecisionVersionBuilder(DecisionVersion instance,Boolean validationEnabled) {
        this.fluent = this; 
        this.withMetadata(instance.getMetadata());
        
        this.withSpec(instance.getSpec());
        
        this.withStatus(instance.getStatus());
        
        this.withKind(instance.getKind());
        
        this.withApiVersion(instance.getApiVersion());
        
        this.validationEnabled = validationEnabled; 
    }

    public DecisionVersion build() {
        DecisionVersion buildable = new DecisionVersion();
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
        DecisionVersionBuilder that = (DecisionVersionBuilder) o;
        if (fluent != null &&fluent != this ? !fluent.equals(that.fluent) :that.fluent != null &&fluent != this ) return false;
        
        if (validationEnabled != null ? !validationEnabled.equals(that.validationEnabled) :that.validationEnabled != null) return false;
        return true;
    }

    public int hashCode() {
        return java.util.Objects.hash(fluent,  validationEnabled,  super.hashCode());
    }

}

package org.kie.baaas.dfs.api;

import io.fabric8.kubernetes.api.builder.VisitableBuilder;
import java.lang.Object;
import java.lang.Boolean;

public class DecisionStatusBuilder extends DecisionStatusFluentImpl<DecisionStatusBuilder> implements VisitableBuilder<DecisionStatus,DecisionStatusBuilder> {

    DecisionStatusFluent<?> fluent;
    Boolean validationEnabled;

    public DecisionStatusBuilder() {
        this(true);
    }

    public DecisionStatusBuilder(Boolean validationEnabled) {
        this(new DecisionStatus(), validationEnabled);
    }

    public DecisionStatusBuilder(DecisionStatusFluent<?> fluent) {
        this(fluent, true);
    }

    public DecisionStatusBuilder(DecisionStatusFluent<?> fluent,Boolean validationEnabled) {
        this(fluent, new DecisionStatus(), validationEnabled);
    }

    public DecisionStatusBuilder(DecisionStatusFluent<?> fluent,DecisionStatus instance) {
        this(fluent, instance, true);
    }

    public DecisionStatusBuilder(DecisionStatusFluent<?> fluent,DecisionStatus instance,Boolean validationEnabled) {
        this.fluent = fluent; 
        fluent.withEndpoint(instance.getEndpoint());
        
        fluent.withVersionId(instance.getVersionId());
        
        this.validationEnabled = validationEnabled; 
    }

    public DecisionStatusBuilder(DecisionStatus instance) {
        this(instance,true);
    }

    public DecisionStatusBuilder(DecisionStatus instance,Boolean validationEnabled) {
        this.fluent = this; 
        this.withEndpoint(instance.getEndpoint());
        
        this.withVersionId(instance.getVersionId());
        
        this.validationEnabled = validationEnabled; 
    }

    public DecisionStatus build() {
        DecisionStatus buildable = new DecisionStatus();
        buildable.setEndpoint(fluent.getEndpoint());
        buildable.setVersionId(fluent.getVersionId());
        return buildable;
    }

    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        DecisionStatusBuilder that = (DecisionStatusBuilder) o;
        if (fluent != null &&fluent != this ? !fluent.equals(that.fluent) :that.fluent != null &&fluent != this ) return false;
        
        if (validationEnabled != null ? !validationEnabled.equals(that.validationEnabled) :that.validationEnabled != null) return false;
        return true;
    }

    public int hashCode() {
        return java.util.Objects.hash(fluent,  validationEnabled,  super.hashCode());
    }

}

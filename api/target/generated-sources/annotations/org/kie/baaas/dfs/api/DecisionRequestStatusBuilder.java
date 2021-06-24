package org.kie.baaas.dfs.api;

import io.fabric8.kubernetes.api.builder.VisitableBuilder;
import java.lang.Object;
import java.lang.Boolean;

public class DecisionRequestStatusBuilder extends DecisionRequestStatusFluentImpl<DecisionRequestStatusBuilder> implements VisitableBuilder<DecisionRequestStatus,DecisionRequestStatusBuilder> {

    DecisionRequestStatusFluent<?> fluent;
    Boolean validationEnabled;

    public DecisionRequestStatusBuilder() {
        this(true);
    }

    public DecisionRequestStatusBuilder(Boolean validationEnabled) {
        this(new DecisionRequestStatus(), validationEnabled);
    }

    public DecisionRequestStatusBuilder(DecisionRequestStatusFluent<?> fluent) {
        this(fluent, true);
    }

    public DecisionRequestStatusBuilder(DecisionRequestStatusFluent<?> fluent,Boolean validationEnabled) {
        this(fluent, new DecisionRequestStatus(), validationEnabled);
    }

    public DecisionRequestStatusBuilder(DecisionRequestStatusFluent<?> fluent,DecisionRequestStatus instance) {
        this(fluent, instance, true);
    }

    public DecisionRequestStatusBuilder(DecisionRequestStatusFluent<?> fluent,DecisionRequestStatus instance,Boolean validationEnabled) {
        this.fluent = fluent; 
        fluent.withVersionRef(instance.getVersionRef());
        
        fluent.withState(instance.getState());
        
        fluent.withReason(instance.getReason());
        
        fluent.withMessage(instance.getMessage());
        
        this.validationEnabled = validationEnabled; 
    }

    public DecisionRequestStatusBuilder(DecisionRequestStatus instance) {
        this(instance,true);
    }

    public DecisionRequestStatusBuilder(DecisionRequestStatus instance,Boolean validationEnabled) {
        this.fluent = this; 
        this.withVersionRef(instance.getVersionRef());
        
        this.withState(instance.getState());
        
        this.withReason(instance.getReason());
        
        this.withMessage(instance.getMessage());
        
        this.validationEnabled = validationEnabled; 
    }

    public DecisionRequestStatus build() {
        DecisionRequestStatus buildable = new DecisionRequestStatus();
        buildable.setVersionRef(fluent.getVersionRef());
        buildable.setState(fluent.getState());
        buildable.setReason(fluent.getReason());
        buildable.setMessage(fluent.getMessage());
        return buildable;
    }

    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        DecisionRequestStatusBuilder that = (DecisionRequestStatusBuilder) o;
        if (fluent != null &&fluent != this ? !fluent.equals(that.fluent) :that.fluent != null &&fluent != this ) return false;
        
        if (validationEnabled != null ? !validationEnabled.equals(that.validationEnabled) :that.validationEnabled != null) return false;
        return true;
    }

    public int hashCode() {
        return java.util.Objects.hash(fluent,  validationEnabled,  super.hashCode());
    }

}

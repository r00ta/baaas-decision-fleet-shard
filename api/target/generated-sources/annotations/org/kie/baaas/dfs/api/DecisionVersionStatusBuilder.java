package org.kie.baaas.dfs.api;

import io.fabric8.kubernetes.api.builder.VisitableBuilder;
import java.lang.Object;
import java.lang.Boolean;

public class DecisionVersionStatusBuilder extends DecisionVersionStatusFluentImpl<DecisionVersionStatusBuilder> implements VisitableBuilder<DecisionVersionStatus,DecisionVersionStatusBuilder> {

    DecisionVersionStatusFluent<?> fluent;
    Boolean validationEnabled;

    public DecisionVersionStatusBuilder() {
        this(true);
    }

    public DecisionVersionStatusBuilder(Boolean validationEnabled) {
        this(new DecisionVersionStatus(), validationEnabled);
    }

    public DecisionVersionStatusBuilder(DecisionVersionStatusFluent<?> fluent) {
        this(fluent, true);
    }

    public DecisionVersionStatusBuilder(DecisionVersionStatusFluent<?> fluent,Boolean validationEnabled) {
        this(fluent, new DecisionVersionStatus(), validationEnabled);
    }

    public DecisionVersionStatusBuilder(DecisionVersionStatusFluent<?> fluent,DecisionVersionStatus instance) {
        this(fluent, instance, true);
    }

    public DecisionVersionStatusBuilder(DecisionVersionStatusFluent<?> fluent,DecisionVersionStatus instance,Boolean validationEnabled) {
        this.fluent = fluent; 
        fluent.withPipelineRef(instance.getPipelineRef());
        
        fluent.withImageRef(instance.getImageRef());
        
        fluent.withKogitoServiceRef(instance.getKogitoServiceRef());
        
        fluent.withEndpoint(instance.getEndpoint());
        
        this.validationEnabled = validationEnabled; 
    }

    public DecisionVersionStatusBuilder(DecisionVersionStatus instance) {
        this(instance,true);
    }

    public DecisionVersionStatusBuilder(DecisionVersionStatus instance,Boolean validationEnabled) {
        this.fluent = this; 
        this.withPipelineRef(instance.getPipelineRef());
        
        this.withImageRef(instance.getImageRef());
        
        this.withKogitoServiceRef(instance.getKogitoServiceRef());
        
        this.withEndpoint(instance.getEndpoint());
        
        this.validationEnabled = validationEnabled; 
    }

    public DecisionVersionStatus build() {
        DecisionVersionStatus buildable = new DecisionVersionStatus();
        buildable.setPipelineRef(fluent.getPipelineRef());
        buildable.setImageRef(fluent.getImageRef());
        buildable.setKogitoServiceRef(fluent.getKogitoServiceRef());
        buildable.setEndpoint(fluent.getEndpoint());
        return buildable;
    }

    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        DecisionVersionStatusBuilder that = (DecisionVersionStatusBuilder) o;
        if (fluent != null &&fluent != this ? !fluent.equals(that.fluent) :that.fluent != null &&fluent != this ) return false;
        
        if (validationEnabled != null ? !validationEnabled.equals(that.validationEnabled) :that.validationEnabled != null) return false;
        return true;
    }

    public int hashCode() {
        return java.util.Objects.hash(fluent,  validationEnabled,  super.hashCode());
    }

}

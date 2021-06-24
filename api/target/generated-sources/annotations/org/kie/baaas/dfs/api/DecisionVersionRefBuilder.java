package org.kie.baaas.dfs.api;

import io.fabric8.kubernetes.api.builder.VisitableBuilder;
import java.lang.Object;
import java.lang.Boolean;

public class DecisionVersionRefBuilder extends DecisionVersionRefFluentImpl<DecisionVersionRefBuilder> implements VisitableBuilder<DecisionVersionRef,DecisionVersionRefBuilder> {

    DecisionVersionRefFluent<?> fluent;
    Boolean validationEnabled;

    public DecisionVersionRefBuilder() {
        this(true);
    }

    public DecisionVersionRefBuilder(Boolean validationEnabled) {
        this(new DecisionVersionRef(), validationEnabled);
    }

    public DecisionVersionRefBuilder(DecisionVersionRefFluent<?> fluent) {
        this(fluent, true);
    }

    public DecisionVersionRefBuilder(DecisionVersionRefFluent<?> fluent,Boolean validationEnabled) {
        this(fluent, new DecisionVersionRef(), validationEnabled);
    }

    public DecisionVersionRefBuilder(DecisionVersionRefFluent<?> fluent,DecisionVersionRef instance) {
        this(fluent, instance, true);
    }

    public DecisionVersionRefBuilder(DecisionVersionRefFluent<?> fluent,DecisionVersionRef instance,Boolean validationEnabled) {
        this.fluent = fluent; 
        fluent.withName(instance.getName());
        
        fluent.withNamespace(instance.getNamespace());
        
        fluent.withVersion(instance.getVersion());
        
        this.validationEnabled = validationEnabled; 
    }

    public DecisionVersionRefBuilder(DecisionVersionRef instance) {
        this(instance,true);
    }

    public DecisionVersionRefBuilder(DecisionVersionRef instance,Boolean validationEnabled) {
        this.fluent = this; 
        this.withName(instance.getName());
        
        this.withNamespace(instance.getNamespace());
        
        this.withVersion(instance.getVersion());
        
        this.validationEnabled = validationEnabled; 
    }

    public DecisionVersionRef build() {
        DecisionVersionRef buildable = new DecisionVersionRef();
        buildable.setName(fluent.getName());
        buildable.setNamespace(fluent.getNamespace());
        buildable.setVersion(fluent.getVersion());
        return buildable;
    }

    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        DecisionVersionRefBuilder that = (DecisionVersionRefBuilder) o;
        if (fluent != null &&fluent != this ? !fluent.equals(that.fluent) :that.fluent != null &&fluent != this ) return false;
        
        if (validationEnabled != null ? !validationEnabled.equals(that.validationEnabled) :that.validationEnabled != null) return false;
        return true;
    }

    public int hashCode() {
        return java.util.Objects.hash(fluent,  validationEnabled,  super.hashCode());
    }

}

package org.kie.baaas.dfs.api;

import io.fabric8.kubernetes.api.builder.VisitableBuilder;
import java.lang.Object;
import java.lang.Boolean;

public class DecisionVersionSpecBuilder extends DecisionVersionSpecFluentImpl<DecisionVersionSpecBuilder> implements VisitableBuilder<DecisionVersionSpec,DecisionVersionSpecBuilder> {

    DecisionVersionSpecFluent<?> fluent;
    Boolean validationEnabled;

    public DecisionVersionSpecBuilder() {
        this(true);
    }

    public DecisionVersionSpecBuilder(Boolean validationEnabled) {
        this(new DecisionVersionSpec(), validationEnabled);
    }

    public DecisionVersionSpecBuilder(DecisionVersionSpecFluent<?> fluent) {
        this(fluent, true);
    }

    public DecisionVersionSpecBuilder(DecisionVersionSpecFluent<?> fluent,Boolean validationEnabled) {
        this(fluent, new DecisionVersionSpec(), validationEnabled);
    }

    public DecisionVersionSpecBuilder(DecisionVersionSpecFluent<?> fluent,DecisionVersionSpec instance) {
        this(fluent, instance, true);
    }

    public DecisionVersionSpecBuilder(DecisionVersionSpecFluent<?> fluent,DecisionVersionSpec instance,Boolean validationEnabled) {
        this.fluent = fluent; 
        fluent.withVersion(instance.getVersion());
        
        fluent.withSource(instance.getSource());
        
        fluent.withKafka(instance.getKafka());
        
        fluent.withEnv(instance.getEnv());
        
        this.validationEnabled = validationEnabled; 
    }

    public DecisionVersionSpecBuilder(DecisionVersionSpec instance) {
        this(instance,true);
    }

    public DecisionVersionSpecBuilder(DecisionVersionSpec instance,Boolean validationEnabled) {
        this.fluent = this; 
        this.withVersion(instance.getVersion());
        
        this.withSource(instance.getSource());
        
        this.withKafka(instance.getKafka());
        
        this.withEnv(instance.getEnv());
        
        this.validationEnabled = validationEnabled; 
    }

    public DecisionVersionSpec build() {
        DecisionVersionSpec buildable = new DecisionVersionSpec();
        buildable.setVersion(fluent.getVersion());
        buildable.setSource(fluent.getSource());
        buildable.setKafka(fluent.getKafka());
        buildable.setEnv(fluent.getEnv());
        return buildable;
    }

    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        DecisionVersionSpecBuilder that = (DecisionVersionSpecBuilder) o;
        if (fluent != null &&fluent != this ? !fluent.equals(that.fluent) :that.fluent != null &&fluent != this ) return false;
        
        if (validationEnabled != null ? !validationEnabled.equals(that.validationEnabled) :that.validationEnabled != null) return false;
        return true;
    }

    public int hashCode() {
        return java.util.Objects.hash(fluent,  validationEnabled,  super.hashCode());
    }

}

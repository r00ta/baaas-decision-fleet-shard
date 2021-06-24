package org.kie.baaas.dfs.api;

import io.fabric8.kubernetes.api.builder.VisitableBuilder;
import java.lang.Object;
import java.lang.Boolean;

public class DecisionRequestSpecBuilder extends DecisionRequestSpecFluentImpl<DecisionRequestSpecBuilder> implements VisitableBuilder<DecisionRequestSpec,DecisionRequestSpecBuilder> {

    DecisionRequestSpecFluent<?> fluent;
    Boolean validationEnabled;

    public DecisionRequestSpecBuilder() {
        this(true);
    }

    public DecisionRequestSpecBuilder(Boolean validationEnabled) {
        this(new DecisionRequestSpec(), validationEnabled);
    }

    public DecisionRequestSpecBuilder(DecisionRequestSpecFluent<?> fluent) {
        this(fluent, true);
    }

    public DecisionRequestSpecBuilder(DecisionRequestSpecFluent<?> fluent,Boolean validationEnabled) {
        this(fluent, new DecisionRequestSpec(), validationEnabled);
    }

    public DecisionRequestSpecBuilder(DecisionRequestSpecFluent<?> fluent,DecisionRequestSpec instance) {
        this(fluent, instance, true);
    }

    public DecisionRequestSpecBuilder(DecisionRequestSpecFluent<?> fluent,DecisionRequestSpec instance,Boolean validationEnabled) {
        this.fluent = fluent; 
        fluent.withCustomerId(instance.getCustomerId());
        
        fluent.withName(instance.getName());
        
        fluent.withVersion(instance.getVersion());
        
        fluent.withSource(instance.getSource());
        
        fluent.withKafka(instance.getKafka());
        
        fluent.withEnv(instance.getEnv());
        
        fluent.withWebhooks(instance.getWebhooks());
        
        this.validationEnabled = validationEnabled; 
    }

    public DecisionRequestSpecBuilder(DecisionRequestSpec instance) {
        this(instance,true);
    }

    public DecisionRequestSpecBuilder(DecisionRequestSpec instance,Boolean validationEnabled) {
        this.fluent = this; 
        this.withCustomerId(instance.getCustomerId());
        
        this.withName(instance.getName());
        
        this.withVersion(instance.getVersion());
        
        this.withSource(instance.getSource());
        
        this.withKafka(instance.getKafka());
        
        this.withEnv(instance.getEnv());
        
        this.withWebhooks(instance.getWebhooks());
        
        this.validationEnabled = validationEnabled; 
    }

    public DecisionRequestSpec build() {
        DecisionRequestSpec buildable = new DecisionRequestSpec();
        buildable.setCustomerId(fluent.getCustomerId());
        buildable.setName(fluent.getName());
        buildable.setVersion(fluent.getVersion());
        buildable.setSource(fluent.getSource());
        buildable.setKafka(fluent.getKafka());
        buildable.setEnv(fluent.getEnv());
        buildable.setWebhooks(fluent.getWebhooks());
        return buildable;
    }

    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        DecisionRequestSpecBuilder that = (DecisionRequestSpecBuilder) o;
        if (fluent != null &&fluent != this ? !fluent.equals(that.fluent) :that.fluent != null &&fluent != this ) return false;
        
        if (validationEnabled != null ? !validationEnabled.equals(that.validationEnabled) :that.validationEnabled != null) return false;
        return true;
    }

    public int hashCode() {
        return java.util.Objects.hash(fluent,  validationEnabled,  super.hashCode());
    }

}

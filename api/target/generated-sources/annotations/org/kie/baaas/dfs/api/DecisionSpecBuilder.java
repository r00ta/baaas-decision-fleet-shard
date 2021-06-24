package org.kie.baaas.dfs.api;

import io.fabric8.kubernetes.api.builder.VisitableBuilder;
import java.lang.Object;
import java.lang.Boolean;

public class DecisionSpecBuilder extends DecisionSpecFluentImpl<DecisionSpecBuilder> implements VisitableBuilder<DecisionSpec,DecisionSpecBuilder> {

    DecisionSpecFluent<?> fluent;
    Boolean validationEnabled;

    public DecisionSpecBuilder() {
        this(true);
    }

    public DecisionSpecBuilder(Boolean validationEnabled) {
        this(new DecisionSpec(), validationEnabled);
    }

    public DecisionSpecBuilder(DecisionSpecFluent<?> fluent) {
        this(fluent, true);
    }

    public DecisionSpecBuilder(DecisionSpecFluent<?> fluent,Boolean validationEnabled) {
        this(fluent, new DecisionSpec(), validationEnabled);
    }

    public DecisionSpecBuilder(DecisionSpecFluent<?> fluent,DecisionSpec instance) {
        this(fluent, instance, true);
    }

    public DecisionSpecBuilder(DecisionSpecFluent<?> fluent,DecisionSpec instance,Boolean validationEnabled) {
        this.fluent = fluent; 
        fluent.withDefinition(instance.getDefinition());
        
        fluent.withWebhooks(instance.getWebhooks());
        
        this.validationEnabled = validationEnabled; 
    }

    public DecisionSpecBuilder(DecisionSpec instance) {
        this(instance,true);
    }

    public DecisionSpecBuilder(DecisionSpec instance,Boolean validationEnabled) {
        this.fluent = this; 
        this.withDefinition(instance.getDefinition());
        
        this.withWebhooks(instance.getWebhooks());
        
        this.validationEnabled = validationEnabled; 
    }

    public DecisionSpec build() {
        DecisionSpec buildable = new DecisionSpec();
        buildable.setDefinition(fluent.getDefinition());
        buildable.setWebhooks(fluent.getWebhooks());
        return buildable;
    }

    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        DecisionSpecBuilder that = (DecisionSpecBuilder) o;
        if (fluent != null &&fluent != this ? !fluent.equals(that.fluent) :that.fluent != null &&fluent != this ) return false;
        
        if (validationEnabled != null ? !validationEnabled.equals(that.validationEnabled) :that.validationEnabled != null) return false;
        return true;
    }

    public int hashCode() {
        return java.util.Objects.hash(fluent,  validationEnabled,  super.hashCode());
    }

}

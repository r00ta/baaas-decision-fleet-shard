package org.kie.baaas.dfs.api;

import io.fabric8.kubernetes.api.builder.VisitableBuilder;
import java.lang.Object;
import java.lang.Boolean;

public class WebhookBuilder extends WebhookFluentImpl<WebhookBuilder> implements VisitableBuilder<Webhook,WebhookBuilder> {

    WebhookFluent<?> fluent;
    Boolean validationEnabled;

    public WebhookBuilder() {
        this(true);
    }

    public WebhookBuilder(Boolean validationEnabled) {
        this(new Webhook(), validationEnabled);
    }

    public WebhookBuilder(WebhookFluent<?> fluent) {
        this(fluent, true);
    }

    public WebhookBuilder(WebhookFluent<?> fluent,Boolean validationEnabled) {
        this(fluent, new Webhook(), validationEnabled);
    }

    public WebhookBuilder(WebhookFluent<?> fluent,Webhook instance) {
        this(fluent, instance, true);
    }

    public WebhookBuilder(WebhookFluent<?> fluent,Webhook instance,Boolean validationEnabled) {
        this.fluent = fluent; 
        fluent.withCustomer(instance.getCustomer());
        
        fluent.withDecision(instance.getDecision());
        
        fluent.withVersion(instance.getVersion());
        
        fluent.withPhase(instance.getPhase());
        
        fluent.withVersionEndpoint(instance.getVersionEndpoint());
        
        fluent.withCurrentEndpoint(instance.getCurrentEndpoint());
        
        fluent.withMessage(instance.getMessage());
        
        fluent.withAt(instance.getAt());
        
        fluent.withNamespace(instance.getNamespace());
        
        fluent.withVersionResource(instance.getVersionResource());
        
        this.validationEnabled = validationEnabled; 
    }

    public WebhookBuilder(Webhook instance) {
        this(instance,true);
    }

    public WebhookBuilder(Webhook instance,Boolean validationEnabled) {
        this.fluent = this; 
        this.withCustomer(instance.getCustomer());
        
        this.withDecision(instance.getDecision());
        
        this.withVersion(instance.getVersion());
        
        this.withPhase(instance.getPhase());
        
        this.withVersionEndpoint(instance.getVersionEndpoint());
        
        this.withCurrentEndpoint(instance.getCurrentEndpoint());
        
        this.withMessage(instance.getMessage());
        
        this.withAt(instance.getAt());
        
        this.withNamespace(instance.getNamespace());
        
        this.withVersionResource(instance.getVersionResource());
        
        this.validationEnabled = validationEnabled; 
    }

    public Webhook build() {
        Webhook buildable = new Webhook();
        buildable.setCustomer(fluent.getCustomer());
        buildable.setDecision(fluent.getDecision());
        buildable.setVersion(fluent.getVersion());
        buildable.setPhase(fluent.getPhase());
        buildable.setVersionEndpoint(fluent.getVersionEndpoint());
        buildable.setCurrentEndpoint(fluent.getCurrentEndpoint());
        buildable.setMessage(fluent.getMessage());
        buildable.setAt(fluent.getAt());
        buildable.setNamespace(fluent.getNamespace());
        buildable.setVersionResource(fluent.getVersionResource());
        return buildable;
    }

    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        WebhookBuilder that = (WebhookBuilder) o;
        if (fluent != null &&fluent != this ? !fluent.equals(that.fluent) :that.fluent != null &&fluent != this ) return false;
        
        if (validationEnabled != null ? !validationEnabled.equals(that.validationEnabled) :that.validationEnabled != null) return false;
        return true;
    }

    public int hashCode() {
        return java.util.Objects.hash(fluent,  validationEnabled,  super.hashCode());
    }

}

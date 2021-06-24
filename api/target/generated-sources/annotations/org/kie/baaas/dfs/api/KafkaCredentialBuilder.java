package org.kie.baaas.dfs.api;

import io.fabric8.kubernetes.api.builder.VisitableBuilder;
import java.lang.Object;
import java.lang.Boolean;

public class KafkaCredentialBuilder extends KafkaCredentialFluentImpl<KafkaCredentialBuilder> implements VisitableBuilder<KafkaCredential,KafkaCredentialBuilder> {

    KafkaCredentialFluent<?> fluent;
    Boolean validationEnabled;

    public KafkaCredentialBuilder() {
        this(true);
    }

    public KafkaCredentialBuilder(Boolean validationEnabled) {
        this(new KafkaCredential(), validationEnabled);
    }

    public KafkaCredentialBuilder(KafkaCredentialFluent<?> fluent) {
        this(fluent, true);
    }

    public KafkaCredentialBuilder(KafkaCredentialFluent<?> fluent,Boolean validationEnabled) {
        this(fluent, new KafkaCredential(), validationEnabled);
    }

    public KafkaCredentialBuilder(KafkaCredentialFluent<?> fluent,KafkaCredential instance) {
        this(fluent, instance, true);
    }

    public KafkaCredentialBuilder(KafkaCredentialFluent<?> fluent,KafkaCredential instance,Boolean validationEnabled) {
        this.fluent = fluent; 
        fluent.withClientId(instance.getClientId());
        
        fluent.withClientSecret(instance.getClientSecret());
        
        this.validationEnabled = validationEnabled; 
    }

    public KafkaCredentialBuilder(KafkaCredential instance) {
        this(instance,true);
    }

    public KafkaCredentialBuilder(KafkaCredential instance,Boolean validationEnabled) {
        this.fluent = this; 
        this.withClientId(instance.getClientId());
        
        this.withClientSecret(instance.getClientSecret());
        
        this.validationEnabled = validationEnabled; 
    }

    public KafkaCredential build() {
        KafkaCredential buildable = new KafkaCredential();
        buildable.setClientId(fluent.getClientId());
        buildable.setClientSecret(fluent.getClientSecret());
        return buildable;
    }

    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        KafkaCredentialBuilder that = (KafkaCredentialBuilder) o;
        if (fluent != null &&fluent != this ? !fluent.equals(that.fluent) :that.fluent != null &&fluent != this ) return false;
        
        if (validationEnabled != null ? !validationEnabled.equals(that.validationEnabled) :that.validationEnabled != null) return false;
        return true;
    }

    public int hashCode() {
        return java.util.Objects.hash(fluent,  validationEnabled,  super.hashCode());
    }

}

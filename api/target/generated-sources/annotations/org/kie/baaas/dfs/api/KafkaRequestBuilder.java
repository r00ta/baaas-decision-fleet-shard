package org.kie.baaas.dfs.api;

import io.fabric8.kubernetes.api.builder.VisitableBuilder;
import java.lang.Object;
import java.lang.Boolean;

public class KafkaRequestBuilder extends KafkaRequestFluentImpl<KafkaRequestBuilder> implements VisitableBuilder<KafkaRequest,KafkaRequestBuilder> {

    KafkaRequestFluent<?> fluent;
    Boolean validationEnabled;

    public KafkaRequestBuilder() {
        this(true);
    }

    public KafkaRequestBuilder(Boolean validationEnabled) {
        this(new KafkaRequest(), validationEnabled);
    }

    public KafkaRequestBuilder(KafkaRequestFluent<?> fluent) {
        this(fluent, true);
    }

    public KafkaRequestBuilder(KafkaRequestFluent<?> fluent,Boolean validationEnabled) {
        this(fluent, new KafkaRequest(), validationEnabled);
    }

    public KafkaRequestBuilder(KafkaRequestFluent<?> fluent,KafkaRequest instance) {
        this(fluent, instance, true);
    }

    public KafkaRequestBuilder(KafkaRequestFluent<?> fluent,KafkaRequest instance,Boolean validationEnabled) {
        this.fluent = fluent; 
        fluent.withBootstrapServers(instance.getBootstrapServers());
        
        fluent.withCredential(instance.getCredential());
        
        fluent.withInputTopic(instance.getInputTopic());
        
        fluent.withOutputTopic(instance.getOutputTopic());
        
        this.validationEnabled = validationEnabled; 
    }

    public KafkaRequestBuilder(KafkaRequest instance) {
        this(instance,true);
    }

    public KafkaRequestBuilder(KafkaRequest instance,Boolean validationEnabled) {
        this.fluent = this; 
        this.withBootstrapServers(instance.getBootstrapServers());
        
        this.withCredential(instance.getCredential());
        
        this.withInputTopic(instance.getInputTopic());
        
        this.withOutputTopic(instance.getOutputTopic());
        
        this.validationEnabled = validationEnabled; 
    }

    public KafkaRequest build() {
        KafkaRequest buildable = new KafkaRequest();
        buildable.setBootstrapServers(fluent.getBootstrapServers());
        buildable.setCredential(fluent.getCredential());
        buildable.setInputTopic(fluent.getInputTopic());
        buildable.setOutputTopic(fluent.getOutputTopic());
        return buildable;
    }

    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        KafkaRequestBuilder that = (KafkaRequestBuilder) o;
        if (fluent != null &&fluent != this ? !fluent.equals(that.fluent) :that.fluent != null &&fluent != this ) return false;
        
        if (validationEnabled != null ? !validationEnabled.equals(that.validationEnabled) :that.validationEnabled != null) return false;
        return true;
    }

    public int hashCode() {
        return java.util.Objects.hash(fluent,  validationEnabled,  super.hashCode());
    }

}

package org.kie.baaas.dfs.api;

import io.fabric8.kubernetes.api.builder.VisitableBuilder;
import java.lang.Object;
import java.lang.Boolean;

public class KafkaBuilder extends KafkaFluentImpl<KafkaBuilder> implements VisitableBuilder<Kafka,KafkaBuilder> {

    KafkaFluent<?> fluent;
    Boolean validationEnabled;

    public KafkaBuilder() {
        this(true);
    }

    public KafkaBuilder(Boolean validationEnabled) {
        this(new Kafka(), validationEnabled);
    }

    public KafkaBuilder(KafkaFluent<?> fluent) {
        this(fluent, true);
    }

    public KafkaBuilder(KafkaFluent<?> fluent,Boolean validationEnabled) {
        this(fluent, new Kafka(), validationEnabled);
    }

    public KafkaBuilder(KafkaFluent<?> fluent,Kafka instance) {
        this(fluent, instance, true);
    }

    public KafkaBuilder(KafkaFluent<?> fluent,Kafka instance,Boolean validationEnabled) {
        this.fluent = fluent; 
        fluent.withBootstrapServers(instance.getBootstrapServers());
        
        fluent.withSecretName(instance.getSecretName());
        
        fluent.withInputTopic(instance.getInputTopic());
        
        fluent.withOutputTopic(instance.getOutputTopic());
        
        this.validationEnabled = validationEnabled; 
    }

    public KafkaBuilder(Kafka instance) {
        this(instance,true);
    }

    public KafkaBuilder(Kafka instance,Boolean validationEnabled) {
        this.fluent = this; 
        this.withBootstrapServers(instance.getBootstrapServers());
        
        this.withSecretName(instance.getSecretName());
        
        this.withInputTopic(instance.getInputTopic());
        
        this.withOutputTopic(instance.getOutputTopic());
        
        this.validationEnabled = validationEnabled; 
    }

    public Kafka build() {
        Kafka buildable = new Kafka();
        buildable.setBootstrapServers(fluent.getBootstrapServers());
        buildable.setSecretName(fluent.getSecretName());
        buildable.setInputTopic(fluent.getInputTopic());
        buildable.setOutputTopic(fluent.getOutputTopic());
        return buildable;
    }

    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        KafkaBuilder that = (KafkaBuilder) o;
        if (fluent != null &&fluent != this ? !fluent.equals(that.fluent) :that.fluent != null &&fluent != this ) return false;
        
        if (validationEnabled != null ? !validationEnabled.equals(that.validationEnabled) :that.validationEnabled != null) return false;
        return true;
    }

    public int hashCode() {
        return java.util.Objects.hash(fluent,  validationEnabled,  super.hashCode());
    }

}

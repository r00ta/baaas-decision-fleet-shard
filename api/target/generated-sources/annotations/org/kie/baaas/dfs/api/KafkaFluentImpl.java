package org.kie.baaas.dfs.api;

import java.lang.StringBuffer;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.lang.StringBuilder;
import io.fabric8.kubernetes.api.builder.BaseFluent;
import java.lang.Object;
import java.lang.String;
import java.lang.Boolean;
import javax.validation.constraints.NotNull;

public class KafkaFluentImpl<A extends KafkaFluent<A>> extends io.fabric8.kubernetes.api.builder.BaseFluent<A> implements KafkaFluent<A> {

    private String bootstrapServers;
    private String secretName;
    private String inputTopic;
    private String outputTopic;

    public KafkaFluentImpl() {
    }

    public KafkaFluentImpl(Kafka instance) {
        this.withBootstrapServers(instance.getBootstrapServers());
        
        this.withSecretName(instance.getSecretName());
        
        this.withInputTopic(instance.getInputTopic());
        
        this.withOutputTopic(instance.getOutputTopic());
    }

    public String getBootstrapServers() {
        return this.bootstrapServers;
    }

    public A withBootstrapServers(String bootstrapServers) {
        this.bootstrapServers=bootstrapServers; return (A) this;
    }

    public Boolean hasBootstrapServers() {
        return this.bootstrapServers != null;
    }

    public A withNewBootstrapServers(String arg1) {
        return (A)withBootstrapServers(new String(arg1));
    }

    public A withNewBootstrapServers(StringBuilder arg1) {
        return (A)withBootstrapServers(new String(arg1));
    }

    public A withNewBootstrapServers(StringBuffer arg1) {
        return (A)withBootstrapServers(new String(arg1));
    }

    public String getSecretName() {
        return this.secretName;
    }

    public A withSecretName(String secretName) {
        this.secretName=secretName; return (A) this;
    }

    public Boolean hasSecretName() {
        return this.secretName != null;
    }

    public A withNewSecretName(String arg1) {
        return (A)withSecretName(new String(arg1));
    }

    public A withNewSecretName(StringBuilder arg1) {
        return (A)withSecretName(new String(arg1));
    }

    public A withNewSecretName(StringBuffer arg1) {
        return (A)withSecretName(new String(arg1));
    }

    public String getInputTopic() {
        return this.inputTopic;
    }

    public A withInputTopic(String inputTopic) {
        this.inputTopic=inputTopic; return (A) this;
    }

    public Boolean hasInputTopic() {
        return this.inputTopic != null;
    }

    public A withNewInputTopic(String arg1) {
        return (A)withInputTopic(new String(arg1));
    }

    public A withNewInputTopic(StringBuilder arg1) {
        return (A)withInputTopic(new String(arg1));
    }

    public A withNewInputTopic(StringBuffer arg1) {
        return (A)withInputTopic(new String(arg1));
    }

    public String getOutputTopic() {
        return this.outputTopic;
    }

    public A withOutputTopic(String outputTopic) {
        this.outputTopic=outputTopic; return (A) this;
    }

    public Boolean hasOutputTopic() {
        return this.outputTopic != null;
    }

    public A withNewOutputTopic(String arg1) {
        return (A)withOutputTopic(new String(arg1));
    }

    public A withNewOutputTopic(StringBuilder arg1) {
        return (A)withOutputTopic(new String(arg1));
    }

    public A withNewOutputTopic(StringBuffer arg1) {
        return (A)withOutputTopic(new String(arg1));
    }

    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        KafkaFluentImpl that = (KafkaFluentImpl) o;
        if (bootstrapServers != null ? !bootstrapServers.equals(that.bootstrapServers) :that.bootstrapServers != null) return false;
        if (secretName != null ? !secretName.equals(that.secretName) :that.secretName != null) return false;
        if (inputTopic != null ? !inputTopic.equals(that.inputTopic) :that.inputTopic != null) return false;
        if (outputTopic != null ? !outputTopic.equals(that.outputTopic) :that.outputTopic != null) return false;
        return true;
    }

    public int hashCode() {
        return java.util.Objects.hash(bootstrapServers,  secretName,  inputTopic,  outputTopic,  super.hashCode());
    }

}

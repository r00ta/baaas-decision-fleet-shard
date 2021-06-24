package org.kie.baaas.dfs.api;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.lang.StringBuilder;
import io.fabric8.kubernetes.api.builder.Nested;
import java.lang.String;
import javax.validation.constraints.NotNull;
import java.lang.StringBuffer;
import java.lang.Deprecated;
import javax.validation.Valid;
import io.fabric8.kubernetes.api.builder.BaseFluent;
import java.lang.Object;
import java.lang.Boolean;

public class KafkaRequestFluentImpl<A extends KafkaRequestFluent<A>> extends io.fabric8.kubernetes.api.builder.BaseFluent<A> implements KafkaRequestFluent<A> {

    private String bootstrapServers;
    private KafkaCredentialBuilder credential;
    private String inputTopic;
    private String outputTopic;

    public KafkaRequestFluentImpl() {
    }

    public KafkaRequestFluentImpl(KafkaRequest instance) {
        this.withBootstrapServers(instance.getBootstrapServers());
        
        this.withCredential(instance.getCredential());
        
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

    
/**
 * This method has been deprecated, please use method buildCredential instead.
 * @return The buildable object.
 */
@Deprecated public KafkaCredential getCredential() {
        return this.credential!=null?this.credential.build():null;
    }

    public KafkaCredential buildCredential() {
        return this.credential!=null?this.credential.build():null;
    }

    public A withCredential(KafkaCredential credential) {
        _visitables.get("credential").remove(this.credential);
        if (credential!=null){ this.credential= new KafkaCredentialBuilder(credential); _visitables.get("credential").add(this.credential);} return (A) this;
    }

    public Boolean hasCredential() {
        return this.credential != null;
    }

    public KafkaRequestFluent.CredentialNested<A> withNewCredential() {
        return new CredentialNestedImpl();
    }

    public KafkaRequestFluent.CredentialNested<A> withNewCredentialLike(KafkaCredential item) {
        return new CredentialNestedImpl(item);
    }

    public KafkaRequestFluent.CredentialNested<A> editCredential() {
        return withNewCredentialLike(getCredential());
    }

    public KafkaRequestFluent.CredentialNested<A> editOrNewCredential() {
        return withNewCredentialLike(getCredential() != null ? getCredential(): new KafkaCredentialBuilder().build());
    }

    public KafkaRequestFluent.CredentialNested<A> editOrNewCredentialLike(KafkaCredential item) {
        return withNewCredentialLike(getCredential() != null ? getCredential(): item);
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
        KafkaRequestFluentImpl that = (KafkaRequestFluentImpl) o;
        if (bootstrapServers != null ? !bootstrapServers.equals(that.bootstrapServers) :that.bootstrapServers != null) return false;
        if (credential != null ? !credential.equals(that.credential) :that.credential != null) return false;
        if (inputTopic != null ? !inputTopic.equals(that.inputTopic) :that.inputTopic != null) return false;
        if (outputTopic != null ? !outputTopic.equals(that.outputTopic) :that.outputTopic != null) return false;
        return true;
    }

    public int hashCode() {
        return java.util.Objects.hash(bootstrapServers,  credential,  inputTopic,  outputTopic,  super.hashCode());
    }

    public class CredentialNestedImpl<N> extends KafkaCredentialFluentImpl<KafkaRequestFluent.CredentialNested<N>> implements KafkaRequestFluent.CredentialNested<N>,io.fabric8.kubernetes.api.builder.Nested<N> {
        private final KafkaCredentialBuilder builder;

            CredentialNestedImpl(KafkaCredential item) {
                this.builder = new KafkaCredentialBuilder(this, item);
                        
            }

            CredentialNestedImpl() {
                this.builder = new KafkaCredentialBuilder(this);
                        
            }

            public N and() {
                return (N) KafkaRequestFluentImpl.this.withCredential(builder.build());
            }

            public N endCredential() {
                return and();
            }
    }


}

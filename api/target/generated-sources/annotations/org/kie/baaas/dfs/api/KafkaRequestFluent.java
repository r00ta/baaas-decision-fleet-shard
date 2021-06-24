package org.kie.baaas.dfs.api;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.lang.StringBuilder;
import io.fabric8.kubernetes.api.builder.Fluent;
import io.fabric8.kubernetes.api.builder.Nested;
import java.lang.String;
import javax.validation.constraints.NotNull;
import java.lang.StringBuffer;
import java.lang.Deprecated;
import javax.validation.Valid;
import java.lang.Boolean;

public interface KafkaRequestFluent<A extends KafkaRequestFluent<A>> extends Fluent<A> {


    public String getBootstrapServers();
    public A withBootstrapServers(String bootstrapServers);
    public Boolean hasBootstrapServers();
    public A withNewBootstrapServers(String arg1);
    public A withNewBootstrapServers(StringBuilder arg1);
    public A withNewBootstrapServers(StringBuffer arg1);
    
/**
 * This method has been deprecated, please use method buildCredential instead.
 * @return The buildable object.
 */
@Deprecated public KafkaCredential getCredential();
    public KafkaCredential buildCredential();
    public A withCredential(KafkaCredential credential);
    public Boolean hasCredential();
    public KafkaRequestFluent.CredentialNested<A> withNewCredential();
    public KafkaRequestFluent.CredentialNested<A> withNewCredentialLike(KafkaCredential item);
    public KafkaRequestFluent.CredentialNested<A> editCredential();
    public KafkaRequestFluent.CredentialNested<A> editOrNewCredential();
    public KafkaRequestFluent.CredentialNested<A> editOrNewCredentialLike(KafkaCredential item);
    public String getInputTopic();
    public A withInputTopic(String inputTopic);
    public Boolean hasInputTopic();
    public A withNewInputTopic(String arg1);
    public A withNewInputTopic(StringBuilder arg1);
    public A withNewInputTopic(StringBuffer arg1);
    public String getOutputTopic();
    public A withOutputTopic(String outputTopic);
    public Boolean hasOutputTopic();
    public A withNewOutputTopic(String arg1);
    public A withNewOutputTopic(StringBuilder arg1);
    public A withNewOutputTopic(StringBuffer arg1);
    public interface CredentialNested<N> extends io.fabric8.kubernetes.api.builder.Nested<N>,KafkaCredentialFluent<KafkaRequestFluent.CredentialNested<N>> {

            public N and();
            public N endCredential();    }


}

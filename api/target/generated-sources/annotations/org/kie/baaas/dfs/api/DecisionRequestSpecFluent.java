package org.kie.baaas.dfs.api;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.lang.StringBuilder;
import io.fabric8.kubernetes.api.builder.Fluent;
import io.fabric8.kubernetes.api.builder.Nested;
import java.lang.String;
import java.net.URI;
import javax.validation.constraints.NotNull;
import java.lang.StringBuffer;
import java.lang.Deprecated;
import javax.validation.Valid;
import io.fabric8.kubernetes.api.model.EnvVar;
import java.util.Collection;
import java.lang.Boolean;

public interface DecisionRequestSpecFluent<A extends DecisionRequestSpecFluent<A>> extends Fluent<A> {


    public String getCustomerId();
    public A withCustomerId(String customerId);
    public Boolean hasCustomerId();
    public A withNewCustomerId(String arg1);
    public A withNewCustomerId(StringBuilder arg1);
    public A withNewCustomerId(StringBuffer arg1);
    public String getName();
    public A withName(String name);
    public Boolean hasName();
    public A withNewName(String arg1);
    public A withNewName(StringBuilder arg1);
    public A withNewName(StringBuffer arg1);
    public String getVersion();
    public A withVersion(String version);
    public Boolean hasVersion();
    public A withNewVersion(String arg1);
    public A withNewVersion(StringBuilder arg1);
    public A withNewVersion(StringBuffer arg1);
    public URI getSource();
    public A withSource(URI source);
    public Boolean hasSource();
    
/**
 * This method has been deprecated, please use method buildKafka instead.
 * @return The buildable object.
 */
@Deprecated public KafkaRequest getKafka();
    public KafkaRequest buildKafka();
    public A withKafka(KafkaRequest kafka);
    public Boolean hasKafka();
    public DecisionRequestSpecFluent.KafkaNested<A> withNewKafka();
    public DecisionRequestSpecFluent.KafkaNested<A> withNewKafkaLike(KafkaRequest item);
    public DecisionRequestSpecFluent.KafkaNested<A> editKafka();
    public DecisionRequestSpecFluent.KafkaNested<A> editOrNewKafka();
    public DecisionRequestSpecFluent.KafkaNested<A> editOrNewKafkaLike(KafkaRequest item);
    public Collection<EnvVar> getEnv();
    public A withEnv(Collection<EnvVar> env);
    public Boolean hasEnv();
    public Collection<URI> getWebhooks();
    public A withWebhooks(Collection<URI> webhooks);
    public Boolean hasWebhooks();
    public interface KafkaNested<N> extends io.fabric8.kubernetes.api.builder.Nested<N>,KafkaRequestFluent<DecisionRequestSpecFluent.KafkaNested<N>> {

            public N and();
            public N endKafka();    }


}

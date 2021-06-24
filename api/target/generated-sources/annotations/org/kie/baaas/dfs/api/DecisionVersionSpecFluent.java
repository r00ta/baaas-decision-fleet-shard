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

public interface DecisionVersionSpecFluent<A extends DecisionVersionSpecFluent<A>> extends Fluent<A> {


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
@Deprecated public Kafka getKafka();
    public Kafka buildKafka();
    public A withKafka(Kafka kafka);
    public Boolean hasKafka();
    public DecisionVersionSpecFluent.KafkaNested<A> withNewKafka();
    public DecisionVersionSpecFluent.KafkaNested<A> withNewKafkaLike(Kafka item);
    public DecisionVersionSpecFluent.KafkaNested<A> editKafka();
    public DecisionVersionSpecFluent.KafkaNested<A> editOrNewKafka();
    public DecisionVersionSpecFluent.KafkaNested<A> editOrNewKafkaLike(Kafka item);
    public Collection<EnvVar> getEnv();
    public A withEnv(Collection<EnvVar> env);
    public Boolean hasEnv();
    public interface KafkaNested<N> extends io.fabric8.kubernetes.api.builder.Nested<N>,KafkaFluent<DecisionVersionSpecFluent.KafkaNested<N>> {

            public N and();
            public N endKafka();    }


}

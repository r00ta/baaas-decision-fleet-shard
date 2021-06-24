package org.kie.baaas.dfs.api;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.lang.StringBuilder;
import io.fabric8.kubernetes.api.builder.Nested;
import java.lang.String;
import java.net.URI;
import javax.validation.constraints.NotNull;
import java.lang.StringBuffer;
import java.lang.Deprecated;
import javax.validation.Valid;
import io.fabric8.kubernetes.api.builder.BaseFluent;
import io.fabric8.kubernetes.api.model.EnvVar;
import java.util.Collection;
import java.lang.Object;
import java.lang.Boolean;

public class DecisionVersionSpecFluentImpl<A extends DecisionVersionSpecFluent<A>> extends io.fabric8.kubernetes.api.builder.BaseFluent<A> implements DecisionVersionSpecFluent<A> {

    private String version;
    private URI source;
    private KafkaBuilder kafka;
    private Collection<EnvVar> env;

    public DecisionVersionSpecFluentImpl() {
    }

    public DecisionVersionSpecFluentImpl(DecisionVersionSpec instance) {
        this.withVersion(instance.getVersion());
        
        this.withSource(instance.getSource());
        
        this.withKafka(instance.getKafka());
        
        this.withEnv(instance.getEnv());
    }

    public String getVersion() {
        return this.version;
    }

    public A withVersion(String version) {
        this.version=version; return (A) this;
    }

    public Boolean hasVersion() {
        return this.version != null;
    }

    public A withNewVersion(String arg1) {
        return (A)withVersion(new String(arg1));
    }

    public A withNewVersion(StringBuilder arg1) {
        return (A)withVersion(new String(arg1));
    }

    public A withNewVersion(StringBuffer arg1) {
        return (A)withVersion(new String(arg1));
    }

    public URI getSource() {
        return this.source;
    }

    public A withSource(URI source) {
        this.source=source; return (A) this;
    }

    public Boolean hasSource() {
        return this.source != null;
    }

    
/**
 * This method has been deprecated, please use method buildKafka instead.
 * @return The buildable object.
 */
@Deprecated public Kafka getKafka() {
        return this.kafka!=null?this.kafka.build():null;
    }

    public Kafka buildKafka() {
        return this.kafka!=null?this.kafka.build():null;
    }

    public A withKafka(Kafka kafka) {
        _visitables.get("kafka").remove(this.kafka);
        if (kafka!=null){ this.kafka= new KafkaBuilder(kafka); _visitables.get("kafka").add(this.kafka);} return (A) this;
    }

    public Boolean hasKafka() {
        return this.kafka != null;
    }

    public DecisionVersionSpecFluent.KafkaNested<A> withNewKafka() {
        return new KafkaNestedImpl();
    }

    public DecisionVersionSpecFluent.KafkaNested<A> withNewKafkaLike(Kafka item) {
        return new KafkaNestedImpl(item);
    }

    public DecisionVersionSpecFluent.KafkaNested<A> editKafka() {
        return withNewKafkaLike(getKafka());
    }

    public DecisionVersionSpecFluent.KafkaNested<A> editOrNewKafka() {
        return withNewKafkaLike(getKafka() != null ? getKafka(): new KafkaBuilder().build());
    }

    public DecisionVersionSpecFluent.KafkaNested<A> editOrNewKafkaLike(Kafka item) {
        return withNewKafkaLike(getKafka() != null ? getKafka(): item);
    }

    public Collection<EnvVar> getEnv() {
        return this.env;
    }

    public A withEnv(Collection<EnvVar> env) {
        this.env=env; return (A) this;
    }

    public Boolean hasEnv() {
        return this.env != null;
    }

    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DecisionVersionSpecFluentImpl that = (DecisionVersionSpecFluentImpl) o;
        if (version != null ? !version.equals(that.version) :that.version != null) return false;
        if (source != null ? !source.equals(that.source) :that.source != null) return false;
        if (kafka != null ? !kafka.equals(that.kafka) :that.kafka != null) return false;
        if (env != null ? !env.equals(that.env) :that.env != null) return false;
        return true;
    }

    public int hashCode() {
        return java.util.Objects.hash(version,  source,  kafka,  env,  super.hashCode());
    }

    public class KafkaNestedImpl<N> extends KafkaFluentImpl<DecisionVersionSpecFluent.KafkaNested<N>> implements DecisionVersionSpecFluent.KafkaNested<N>,io.fabric8.kubernetes.api.builder.Nested<N> {
        private final KafkaBuilder builder;

            KafkaNestedImpl(Kafka item) {
                this.builder = new KafkaBuilder(this, item);
                        
            }

            KafkaNestedImpl() {
                this.builder = new KafkaBuilder(this);
                        
            }

            public N and() {
                return (N) DecisionVersionSpecFluentImpl.this.withKafka(builder.build());
            }

            public N endKafka() {
                return and();
            }
    }


}

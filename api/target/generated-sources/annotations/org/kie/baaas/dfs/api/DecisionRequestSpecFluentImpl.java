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

public class DecisionRequestSpecFluentImpl<A extends DecisionRequestSpecFluent<A>> extends io.fabric8.kubernetes.api.builder.BaseFluent<A> implements DecisionRequestSpecFluent<A> {

    private String customerId;
    private String name;
    private String version;
    private URI source;
    private KafkaRequestBuilder kafka;
    private Collection<EnvVar> env;
    private Collection<URI> webhooks;

    public DecisionRequestSpecFluentImpl() {
    }

    public DecisionRequestSpecFluentImpl(DecisionRequestSpec instance) {
        this.withCustomerId(instance.getCustomerId());
        
        this.withName(instance.getName());
        
        this.withVersion(instance.getVersion());
        
        this.withSource(instance.getSource());
        
        this.withKafka(instance.getKafka());
        
        this.withEnv(instance.getEnv());
        
        this.withWebhooks(instance.getWebhooks());
    }

    public String getCustomerId() {
        return this.customerId;
    }

    public A withCustomerId(String customerId) {
        this.customerId=customerId; return (A) this;
    }

    public Boolean hasCustomerId() {
        return this.customerId != null;
    }

    public A withNewCustomerId(String arg1) {
        return (A)withCustomerId(new String(arg1));
    }

    public A withNewCustomerId(StringBuilder arg1) {
        return (A)withCustomerId(new String(arg1));
    }

    public A withNewCustomerId(StringBuffer arg1) {
        return (A)withCustomerId(new String(arg1));
    }

    public String getName() {
        return this.name;
    }

    public A withName(String name) {
        this.name=name; return (A) this;
    }

    public Boolean hasName() {
        return this.name != null;
    }

    public A withNewName(String arg1) {
        return (A)withName(new String(arg1));
    }

    public A withNewName(StringBuilder arg1) {
        return (A)withName(new String(arg1));
    }

    public A withNewName(StringBuffer arg1) {
        return (A)withName(new String(arg1));
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
@Deprecated public KafkaRequest getKafka() {
        return this.kafka!=null?this.kafka.build():null;
    }

    public KafkaRequest buildKafka() {
        return this.kafka!=null?this.kafka.build():null;
    }

    public A withKafka(KafkaRequest kafka) {
        _visitables.get("kafka").remove(this.kafka);
        if (kafka!=null){ this.kafka= new KafkaRequestBuilder(kafka); _visitables.get("kafka").add(this.kafka);} return (A) this;
    }

    public Boolean hasKafka() {
        return this.kafka != null;
    }

    public DecisionRequestSpecFluent.KafkaNested<A> withNewKafka() {
        return new KafkaNestedImpl();
    }

    public DecisionRequestSpecFluent.KafkaNested<A> withNewKafkaLike(KafkaRequest item) {
        return new KafkaNestedImpl(item);
    }

    public DecisionRequestSpecFluent.KafkaNested<A> editKafka() {
        return withNewKafkaLike(getKafka());
    }

    public DecisionRequestSpecFluent.KafkaNested<A> editOrNewKafka() {
        return withNewKafkaLike(getKafka() != null ? getKafka(): new KafkaRequestBuilder().build());
    }

    public DecisionRequestSpecFluent.KafkaNested<A> editOrNewKafkaLike(KafkaRequest item) {
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

    public Collection<URI> getWebhooks() {
        return this.webhooks;
    }

    public A withWebhooks(Collection<URI> webhooks) {
        this.webhooks=webhooks; return (A) this;
    }

    public Boolean hasWebhooks() {
        return this.webhooks != null;
    }

    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DecisionRequestSpecFluentImpl that = (DecisionRequestSpecFluentImpl) o;
        if (customerId != null ? !customerId.equals(that.customerId) :that.customerId != null) return false;
        if (name != null ? !name.equals(that.name) :that.name != null) return false;
        if (version != null ? !version.equals(that.version) :that.version != null) return false;
        if (source != null ? !source.equals(that.source) :that.source != null) return false;
        if (kafka != null ? !kafka.equals(that.kafka) :that.kafka != null) return false;
        if (env != null ? !env.equals(that.env) :that.env != null) return false;
        if (webhooks != null ? !webhooks.equals(that.webhooks) :that.webhooks != null) return false;
        return true;
    }

    public int hashCode() {
        return java.util.Objects.hash(customerId,  name,  version,  source,  kafka,  env,  webhooks,  super.hashCode());
    }

    public class KafkaNestedImpl<N> extends KafkaRequestFluentImpl<DecisionRequestSpecFluent.KafkaNested<N>> implements DecisionRequestSpecFluent.KafkaNested<N>,io.fabric8.kubernetes.api.builder.Nested<N> {
        private final KafkaRequestBuilder builder;

            KafkaNestedImpl(KafkaRequest item) {
                this.builder = new KafkaRequestBuilder(this, item);
                        
            }

            KafkaNestedImpl() {
                this.builder = new KafkaRequestBuilder(this);
                        
            }

            public N and() {
                return (N) DecisionRequestSpecFluentImpl.this.withKafka(builder.build());
            }

            public N endKafka() {
                return and();
            }
    }


}

package org.kie.baaas.dfs.api;

import java.lang.StringBuffer;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.lang.StringBuilder;
import io.fabric8.kubernetes.api.builder.BaseFluent;
import java.lang.Object;
import java.lang.String;
import java.lang.Boolean;
import javax.validation.constraints.NotNull;

public class KafkaCredentialFluentImpl<A extends KafkaCredentialFluent<A>> extends io.fabric8.kubernetes.api.builder.BaseFluent<A> implements KafkaCredentialFluent<A> {

    private String clientId;
    private String clientSecret;

    public KafkaCredentialFluentImpl() {
    }

    public KafkaCredentialFluentImpl(KafkaCredential instance) {
        this.withClientId(instance.getClientId());
        
        this.withClientSecret(instance.getClientSecret());
    }

    public String getClientId() {
        return this.clientId;
    }

    public A withClientId(String clientId) {
        this.clientId=clientId; return (A) this;
    }

    public Boolean hasClientId() {
        return this.clientId != null;
    }

    public A withNewClientId(String arg1) {
        return (A)withClientId(new String(arg1));
    }

    public A withNewClientId(StringBuilder arg1) {
        return (A)withClientId(new String(arg1));
    }

    public A withNewClientId(StringBuffer arg1) {
        return (A)withClientId(new String(arg1));
    }

    public String getClientSecret() {
        return this.clientSecret;
    }

    public A withClientSecret(String clientSecret) {
        this.clientSecret=clientSecret; return (A) this;
    }

    public Boolean hasClientSecret() {
        return this.clientSecret != null;
    }

    public A withNewClientSecret(String arg1) {
        return (A)withClientSecret(new String(arg1));
    }

    public A withNewClientSecret(StringBuilder arg1) {
        return (A)withClientSecret(new String(arg1));
    }

    public A withNewClientSecret(StringBuffer arg1) {
        return (A)withClientSecret(new String(arg1));
    }

    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        KafkaCredentialFluentImpl that = (KafkaCredentialFluentImpl) o;
        if (clientId != null ? !clientId.equals(that.clientId) :that.clientId != null) return false;
        if (clientSecret != null ? !clientSecret.equals(that.clientSecret) :that.clientSecret != null) return false;
        return true;
    }

    public int hashCode() {
        return java.util.Objects.hash(clientId,  clientSecret,  super.hashCode());
    }

}

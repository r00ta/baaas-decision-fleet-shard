package org.kie.baaas.dfs.api;

import java.lang.StringBuffer;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.lang.StringBuilder;
import io.fabric8.kubernetes.api.builder.Fluent;
import java.lang.String;
import java.lang.Boolean;
import javax.validation.constraints.NotNull;

public interface KafkaCredentialFluent<A extends KafkaCredentialFluent<A>> extends Fluent<A> {


    public String getClientId();
    public A withClientId(String clientId);
    public Boolean hasClientId();
    public A withNewClientId(String arg1);
    public A withNewClientId(StringBuilder arg1);
    public A withNewClientId(StringBuffer arg1);
    public String getClientSecret();
    public A withClientSecret(String clientSecret);
    public Boolean hasClientSecret();
    public A withNewClientSecret(String arg1);
    public A withNewClientSecret(StringBuilder arg1);
    public A withNewClientSecret(StringBuffer arg1);
}

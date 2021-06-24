package org.kie.baaas.dfs.api;

import java.lang.StringBuffer;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.lang.StringBuilder;
import io.fabric8.kubernetes.api.builder.Fluent;
import java.lang.String;
import java.lang.Boolean;
import java.net.URI;
import javax.validation.constraints.NotNull;

public interface WebhookFluent<A extends WebhookFluent<A>> extends Fluent<A> {


    public String getCustomer();
    public A withCustomer(String customer);
    public Boolean hasCustomer();
    public A withNewCustomer(String arg1);
    public A withNewCustomer(StringBuilder arg1);
    public A withNewCustomer(StringBuffer arg1);
    public String getDecision();
    public A withDecision(String decision);
    public Boolean hasDecision();
    public A withNewDecision(String arg1);
    public A withNewDecision(StringBuilder arg1);
    public A withNewDecision(StringBuffer arg1);
    public String getVersion();
    public A withVersion(String version);
    public Boolean hasVersion();
    public A withNewVersion(String arg1);
    public A withNewVersion(StringBuilder arg1);
    public A withNewVersion(StringBuffer arg1);
    public Phase getPhase();
    public A withPhase(Phase phase);
    public Boolean hasPhase();
    public URI getVersionEndpoint();
    public A withVersionEndpoint(URI versionEndpoint);
    public Boolean hasVersionEndpoint();
    public URI getCurrentEndpoint();
    public A withCurrentEndpoint(URI currentEndpoint);
    public Boolean hasCurrentEndpoint();
    public String getMessage();
    public A withMessage(String message);
    public Boolean hasMessage();
    public A withNewMessage(String arg1);
    public A withNewMessage(StringBuilder arg1);
    public A withNewMessage(StringBuffer arg1);
    public String getAt();
    public A withAt(String at);
    public Boolean hasAt();
    public A withNewAt(String arg1);
    public A withNewAt(StringBuilder arg1);
    public A withNewAt(StringBuffer arg1);
    public String getNamespace();
    public A withNamespace(String namespace);
    public Boolean hasNamespace();
    public A withNewNamespace(String arg1);
    public A withNewNamespace(StringBuilder arg1);
    public A withNewNamespace(StringBuffer arg1);
    public String getVersionResource();
    public A withVersionResource(String versionResource);
    public Boolean hasVersionResource();
    public A withNewVersionResource(String arg1);
    public A withNewVersionResource(StringBuilder arg1);
    public A withNewVersionResource(StringBuffer arg1);
}

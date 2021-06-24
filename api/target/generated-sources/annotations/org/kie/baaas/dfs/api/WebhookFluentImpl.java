package org.kie.baaas.dfs.api;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.lang.StringBuilder;
import java.lang.String;
import java.net.URI;
import javax.validation.constraints.NotNull;
import java.lang.StringBuffer;
import io.fabric8.kubernetes.api.builder.BaseFluent;
import java.lang.Object;
import java.lang.Boolean;

public class WebhookFluentImpl<A extends WebhookFluent<A>> extends io.fabric8.kubernetes.api.builder.BaseFluent<A> implements WebhookFluent<A> {

    private String customer;
    private String decision;
    private String version;
    private Phase phase;
    private URI versionEndpoint;
    private URI currentEndpoint;
    private String message;
    private String at;
    private String namespace;
    private String versionResource;

    public WebhookFluentImpl() {
    }

    public WebhookFluentImpl(Webhook instance) {
        this.withCustomer(instance.getCustomer());
        
        this.withDecision(instance.getDecision());
        
        this.withVersion(instance.getVersion());
        
        this.withPhase(instance.getPhase());
        
        this.withVersionEndpoint(instance.getVersionEndpoint());
        
        this.withCurrentEndpoint(instance.getCurrentEndpoint());
        
        this.withMessage(instance.getMessage());
        
        this.withAt(instance.getAt());
        
        this.withNamespace(instance.getNamespace());
        
        this.withVersionResource(instance.getVersionResource());
    }

    public String getCustomer() {
        return this.customer;
    }

    public A withCustomer(String customer) {
        this.customer=customer; return (A) this;
    }

    public Boolean hasCustomer() {
        return this.customer != null;
    }

    public A withNewCustomer(String arg1) {
        return (A)withCustomer(new String(arg1));
    }

    public A withNewCustomer(StringBuilder arg1) {
        return (A)withCustomer(new String(arg1));
    }

    public A withNewCustomer(StringBuffer arg1) {
        return (A)withCustomer(new String(arg1));
    }

    public String getDecision() {
        return this.decision;
    }

    public A withDecision(String decision) {
        this.decision=decision; return (A) this;
    }

    public Boolean hasDecision() {
        return this.decision != null;
    }

    public A withNewDecision(String arg1) {
        return (A)withDecision(new String(arg1));
    }

    public A withNewDecision(StringBuilder arg1) {
        return (A)withDecision(new String(arg1));
    }

    public A withNewDecision(StringBuffer arg1) {
        return (A)withDecision(new String(arg1));
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

    public Phase getPhase() {
        return this.phase;
    }

    public A withPhase(Phase phase) {
        this.phase=phase; return (A) this;
    }

    public Boolean hasPhase() {
        return this.phase != null;
    }

    public URI getVersionEndpoint() {
        return this.versionEndpoint;
    }

    public A withVersionEndpoint(URI versionEndpoint) {
        this.versionEndpoint=versionEndpoint; return (A) this;
    }

    public Boolean hasVersionEndpoint() {
        return this.versionEndpoint != null;
    }

    public URI getCurrentEndpoint() {
        return this.currentEndpoint;
    }

    public A withCurrentEndpoint(URI currentEndpoint) {
        this.currentEndpoint=currentEndpoint; return (A) this;
    }

    public Boolean hasCurrentEndpoint() {
        return this.currentEndpoint != null;
    }

    public String getMessage() {
        return this.message;
    }

    public A withMessage(String message) {
        this.message=message; return (A) this;
    }

    public Boolean hasMessage() {
        return this.message != null;
    }

    public A withNewMessage(String arg1) {
        return (A)withMessage(new String(arg1));
    }

    public A withNewMessage(StringBuilder arg1) {
        return (A)withMessage(new String(arg1));
    }

    public A withNewMessage(StringBuffer arg1) {
        return (A)withMessage(new String(arg1));
    }

    public String getAt() {
        return this.at;
    }

    public A withAt(String at) {
        this.at=at; return (A) this;
    }

    public Boolean hasAt() {
        return this.at != null;
    }

    public A withNewAt(String arg1) {
        return (A)withAt(new String(arg1));
    }

    public A withNewAt(StringBuilder arg1) {
        return (A)withAt(new String(arg1));
    }

    public A withNewAt(StringBuffer arg1) {
        return (A)withAt(new String(arg1));
    }

    public String getNamespace() {
        return this.namespace;
    }

    public A withNamespace(String namespace) {
        this.namespace=namespace; return (A) this;
    }

    public Boolean hasNamespace() {
        return this.namespace != null;
    }

    public A withNewNamespace(String arg1) {
        return (A)withNamespace(new String(arg1));
    }

    public A withNewNamespace(StringBuilder arg1) {
        return (A)withNamespace(new String(arg1));
    }

    public A withNewNamespace(StringBuffer arg1) {
        return (A)withNamespace(new String(arg1));
    }

    public String getVersionResource() {
        return this.versionResource;
    }

    public A withVersionResource(String versionResource) {
        this.versionResource=versionResource; return (A) this;
    }

    public Boolean hasVersionResource() {
        return this.versionResource != null;
    }

    public A withNewVersionResource(String arg1) {
        return (A)withVersionResource(new String(arg1));
    }

    public A withNewVersionResource(StringBuilder arg1) {
        return (A)withVersionResource(new String(arg1));
    }

    public A withNewVersionResource(StringBuffer arg1) {
        return (A)withVersionResource(new String(arg1));
    }

    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        WebhookFluentImpl that = (WebhookFluentImpl) o;
        if (customer != null ? !customer.equals(that.customer) :that.customer != null) return false;
        if (decision != null ? !decision.equals(that.decision) :that.decision != null) return false;
        if (version != null ? !version.equals(that.version) :that.version != null) return false;
        if (phase != null ? !phase.equals(that.phase) :that.phase != null) return false;
        if (versionEndpoint != null ? !versionEndpoint.equals(that.versionEndpoint) :that.versionEndpoint != null) return false;
        if (currentEndpoint != null ? !currentEndpoint.equals(that.currentEndpoint) :that.currentEndpoint != null) return false;
        if (message != null ? !message.equals(that.message) :that.message != null) return false;
        if (at != null ? !at.equals(that.at) :that.at != null) return false;
        if (namespace != null ? !namespace.equals(that.namespace) :that.namespace != null) return false;
        if (versionResource != null ? !versionResource.equals(that.versionResource) :that.versionResource != null) return false;
        return true;
    }

    public int hashCode() {
        return java.util.Objects.hash(customer,  decision,  version,  phase,  versionEndpoint,  currentEndpoint,  message,  at,  namespace,  versionResource,  super.hashCode());
    }

}

package org.kie.baaas.dfs.api;

import java.lang.StringBuffer;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.lang.StringBuilder;
import io.fabric8.kubernetes.api.builder.BaseFluent;
import java.lang.Object;
import java.lang.String;
import io.dekorate.crd.annotation.PrinterColumn;
import java.lang.Boolean;
import java.net.URI;

public class DecisionStatusFluentImpl<A extends DecisionStatusFluent<A>> extends io.fabric8.kubernetes.api.builder.BaseFluent<A> implements DecisionStatusFluent<A> {

    private URI endpoint;
    private String versionId;

    public DecisionStatusFluentImpl() {
    }

    public DecisionStatusFluentImpl(DecisionStatus instance) {
        this.withEndpoint(instance.getEndpoint());
        
        this.withVersionId(instance.getVersionId());
    }

    public URI getEndpoint() {
        return this.endpoint;
    }

    public A withEndpoint(URI endpoint) {
        this.endpoint=endpoint; return (A) this;
    }

    public Boolean hasEndpoint() {
        return this.endpoint != null;
    }

    public String getVersionId() {
        return this.versionId;
    }

    public A withVersionId(String versionId) {
        this.versionId=versionId; return (A) this;
    }

    public Boolean hasVersionId() {
        return this.versionId != null;
    }

    public A withNewVersionId(String arg1) {
        return (A)withVersionId(new String(arg1));
    }

    public A withNewVersionId(StringBuilder arg1) {
        return (A)withVersionId(new String(arg1));
    }

    public A withNewVersionId(StringBuffer arg1) {
        return (A)withVersionId(new String(arg1));
    }

    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DecisionStatusFluentImpl that = (DecisionStatusFluentImpl) o;
        if (endpoint != null ? !endpoint.equals(that.endpoint) :that.endpoint != null) return false;
        if (versionId != null ? !versionId.equals(that.versionId) :that.versionId != null) return false;
        return true;
    }

    public int hashCode() {
        return java.util.Objects.hash(endpoint,  versionId,  super.hashCode());
    }

}

package org.kie.baaas.dfs.api;

import java.lang.StringBuffer;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.lang.StringBuilder;
import io.fabric8.kubernetes.api.builder.BaseFluent;
import java.lang.Object;
import java.lang.String;
import java.lang.Boolean;
import java.net.URI;

public class DecisionVersionStatusFluentImpl<A extends DecisionVersionStatusFluent<A>> extends io.fabric8.kubernetes.api.builder.BaseFluent<A> implements DecisionVersionStatusFluent<A> {

    private String pipelineRef;
    private String imageRef;
    private String kogitoServiceRef;
    private URI endpoint;

    public DecisionVersionStatusFluentImpl() {
    }

    public DecisionVersionStatusFluentImpl(DecisionVersionStatus instance) {
        this.withPipelineRef(instance.getPipelineRef());
        
        this.withImageRef(instance.getImageRef());
        
        this.withKogitoServiceRef(instance.getKogitoServiceRef());
        
        this.withEndpoint(instance.getEndpoint());
    }

    public String getPipelineRef() {
        return this.pipelineRef;
    }

    public A withPipelineRef(String pipelineRef) {
        this.pipelineRef=pipelineRef; return (A) this;
    }

    public Boolean hasPipelineRef() {
        return this.pipelineRef != null;
    }

    public A withNewPipelineRef(String arg1) {
        return (A)withPipelineRef(new String(arg1));
    }

    public A withNewPipelineRef(StringBuilder arg1) {
        return (A)withPipelineRef(new String(arg1));
    }

    public A withNewPipelineRef(StringBuffer arg1) {
        return (A)withPipelineRef(new String(arg1));
    }

    public String getImageRef() {
        return this.imageRef;
    }

    public A withImageRef(String imageRef) {
        this.imageRef=imageRef; return (A) this;
    }

    public Boolean hasImageRef() {
        return this.imageRef != null;
    }

    public A withNewImageRef(String arg1) {
        return (A)withImageRef(new String(arg1));
    }

    public A withNewImageRef(StringBuilder arg1) {
        return (A)withImageRef(new String(arg1));
    }

    public A withNewImageRef(StringBuffer arg1) {
        return (A)withImageRef(new String(arg1));
    }

    public String getKogitoServiceRef() {
        return this.kogitoServiceRef;
    }

    public A withKogitoServiceRef(String kogitoServiceRef) {
        this.kogitoServiceRef=kogitoServiceRef; return (A) this;
    }

    public Boolean hasKogitoServiceRef() {
        return this.kogitoServiceRef != null;
    }

    public A withNewKogitoServiceRef(String arg1) {
        return (A)withKogitoServiceRef(new String(arg1));
    }

    public A withNewKogitoServiceRef(StringBuilder arg1) {
        return (A)withKogitoServiceRef(new String(arg1));
    }

    public A withNewKogitoServiceRef(StringBuffer arg1) {
        return (A)withKogitoServiceRef(new String(arg1));
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

    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DecisionVersionStatusFluentImpl that = (DecisionVersionStatusFluentImpl) o;
        if (pipelineRef != null ? !pipelineRef.equals(that.pipelineRef) :that.pipelineRef != null) return false;
        if (imageRef != null ? !imageRef.equals(that.imageRef) :that.imageRef != null) return false;
        if (kogitoServiceRef != null ? !kogitoServiceRef.equals(that.kogitoServiceRef) :that.kogitoServiceRef != null) return false;
        if (endpoint != null ? !endpoint.equals(that.endpoint) :that.endpoint != null) return false;
        return true;
    }

    public int hashCode() {
        return java.util.Objects.hash(pipelineRef,  imageRef,  kogitoServiceRef,  endpoint,  super.hashCode());
    }

}

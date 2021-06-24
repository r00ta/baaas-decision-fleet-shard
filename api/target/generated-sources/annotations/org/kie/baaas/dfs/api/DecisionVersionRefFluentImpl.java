package org.kie.baaas.dfs.api;

import java.lang.StringBuffer;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.lang.StringBuilder;
import io.fabric8.kubernetes.api.builder.BaseFluent;
import java.lang.Object;
import java.lang.String;
import java.lang.Boolean;
import javax.validation.constraints.NotNull;

public class DecisionVersionRefFluentImpl<A extends DecisionVersionRefFluent<A>> extends io.fabric8.kubernetes.api.builder.BaseFluent<A> implements DecisionVersionRefFluent<A> {

    private String name;
    private String namespace;
    private String version;

    public DecisionVersionRefFluentImpl() {
    }

    public DecisionVersionRefFluentImpl(DecisionVersionRef instance) {
        this.withName(instance.getName());
        
        this.withNamespace(instance.getNamespace());
        
        this.withVersion(instance.getVersion());
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

    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DecisionVersionRefFluentImpl that = (DecisionVersionRefFluentImpl) o;
        if (name != null ? !name.equals(that.name) :that.name != null) return false;
        if (namespace != null ? !namespace.equals(that.namespace) :that.namespace != null) return false;
        if (version != null ? !version.equals(that.version) :that.version != null) return false;
        return true;
    }

    public int hashCode() {
        return java.util.Objects.hash(name,  namespace,  version,  super.hashCode());
    }

}

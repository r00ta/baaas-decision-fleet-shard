package org.kie.baaas.dfs.api;

import java.lang.StringBuffer;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.lang.StringBuilder;
import io.fabric8.kubernetes.api.builder.Fluent;
import java.lang.String;
import java.lang.Boolean;
import javax.validation.constraints.NotNull;

public interface DecisionVersionRefFluent<A extends DecisionVersionRefFluent<A>> extends Fluent<A> {


    public String getName();
    public A withName(String name);
    public Boolean hasName();
    public A withNewName(String arg1);
    public A withNewName(StringBuilder arg1);
    public A withNewName(StringBuffer arg1);
    public String getNamespace();
    public A withNamespace(String namespace);
    public Boolean hasNamespace();
    public A withNewNamespace(String arg1);
    public A withNewNamespace(StringBuilder arg1);
    public A withNewNamespace(StringBuffer arg1);
    public String getVersion();
    public A withVersion(String version);
    public Boolean hasVersion();
    public A withNewVersion(String arg1);
    public A withNewVersion(StringBuilder arg1);
    public A withNewVersion(StringBuffer arg1);
}

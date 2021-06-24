package org.kie.baaas.dfs.api;

import java.lang.StringBuffer;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.lang.StringBuilder;
import io.fabric8.kubernetes.api.builder.Fluent;
import java.lang.String;
import io.dekorate.crd.annotation.PrinterColumn;
import java.lang.Boolean;
import java.net.URI;

public interface DecisionStatusFluent<A extends DecisionStatusFluent<A>> extends Fluent<A> {


    public URI getEndpoint();
    public A withEndpoint(URI endpoint);
    public Boolean hasEndpoint();
    public String getVersionId();
    public A withVersionId(String versionId);
    public Boolean hasVersionId();
    public A withNewVersionId(String arg1);
    public A withNewVersionId(StringBuilder arg1);
    public A withNewVersionId(StringBuffer arg1);
}

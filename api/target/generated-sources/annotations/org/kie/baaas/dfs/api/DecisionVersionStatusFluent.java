package org.kie.baaas.dfs.api;

import io.fabric8.kubernetes.api.model.Condition;
import java.lang.StringBuffer;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.lang.StringBuilder;
import io.fabric8.kubernetes.api.builder.Fluent;
import java.lang.String;
import java.lang.Boolean;
import java.net.URI;
import java.util.Map;

public interface DecisionVersionStatusFluent<A extends DecisionVersionStatusFluent<A>> extends Fluent<A> {


    public String getPipelineRef();
    public A withPipelineRef(String pipelineRef);
    public Boolean hasPipelineRef();
    public A withNewPipelineRef(String arg1);
    public A withNewPipelineRef(StringBuilder arg1);
    public A withNewPipelineRef(StringBuffer arg1);
    public String getImageRef();
    public A withImageRef(String imageRef);
    public Boolean hasImageRef();
    public A withNewImageRef(String arg1);
    public A withNewImageRef(StringBuilder arg1);
    public A withNewImageRef(StringBuffer arg1);
    public String getKogitoServiceRef();
    public A withKogitoServiceRef(String kogitoServiceRef);
    public Boolean hasKogitoServiceRef();
    public A withNewKogitoServiceRef(String arg1);
    public A withNewKogitoServiceRef(StringBuilder arg1);
    public A withNewKogitoServiceRef(StringBuffer arg1);
    public URI getEndpoint();
    public A withEndpoint(URI endpoint);
    public Boolean hasEndpoint();
}

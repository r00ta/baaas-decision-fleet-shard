package org.kie.baaas.dfs.api;

import java.lang.StringBuffer;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.lang.StringBuilder;
import io.fabric8.kubernetes.api.builder.Fluent;
import java.lang.String;
import java.lang.Boolean;
import javax.validation.constraints.NotNull;

public interface KafkaFluent<A extends KafkaFluent<A>> extends Fluent<A> {


    public String getBootstrapServers();
    public A withBootstrapServers(String bootstrapServers);
    public Boolean hasBootstrapServers();
    public A withNewBootstrapServers(String arg1);
    public A withNewBootstrapServers(StringBuilder arg1);
    public A withNewBootstrapServers(StringBuffer arg1);
    public String getSecretName();
    public A withSecretName(String secretName);
    public Boolean hasSecretName();
    public A withNewSecretName(String arg1);
    public A withNewSecretName(StringBuilder arg1);
    public A withNewSecretName(StringBuffer arg1);
    public String getInputTopic();
    public A withInputTopic(String inputTopic);
    public Boolean hasInputTopic();
    public A withNewInputTopic(String arg1);
    public A withNewInputTopic(StringBuilder arg1);
    public A withNewInputTopic(StringBuffer arg1);
    public String getOutputTopic();
    public A withOutputTopic(String outputTopic);
    public Boolean hasOutputTopic();
    public A withNewOutputTopic(String arg1);
    public A withNewOutputTopic(StringBuilder arg1);
    public A withNewOutputTopic(StringBuffer arg1);
}

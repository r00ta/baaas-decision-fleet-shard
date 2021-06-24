package org.kie.baaas.dfs.api;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.lang.StringBuilder;
import io.fabric8.kubernetes.api.builder.Fluent;
import io.fabric8.kubernetes.api.builder.Nested;
import java.lang.String;
import java.lang.StringBuffer;
import java.lang.Deprecated;
import io.dekorate.crd.annotation.PrinterColumn;
import java.lang.Boolean;

public interface DecisionRequestStatusFluent<A extends DecisionRequestStatusFluent<A>> extends Fluent<A> {


    
/**
 * This method has been deprecated, please use method buildVersionRef instead.
 * @return The buildable object.
 */
@Deprecated public DecisionVersionRef getVersionRef();
    public DecisionVersionRef buildVersionRef();
    public A withVersionRef(DecisionVersionRef versionRef);
    public Boolean hasVersionRef();
    public DecisionRequestStatusFluent.VersionRefNested<A> withNewVersionRef();
    public DecisionRequestStatusFluent.VersionRefNested<A> withNewVersionRefLike(DecisionVersionRef item);
    public DecisionRequestStatusFluent.VersionRefNested<A> editVersionRef();
    public DecisionRequestStatusFluent.VersionRefNested<A> editOrNewVersionRef();
    public DecisionRequestStatusFluent.VersionRefNested<A> editOrNewVersionRefLike(DecisionVersionRef item);
    public AdmissionStatus getState();
    public A withState(AdmissionStatus state);
    public Boolean hasState();
    public String getReason();
    public A withReason(String reason);
    public Boolean hasReason();
    public A withNewReason(String arg1);
    public A withNewReason(StringBuilder arg1);
    public A withNewReason(StringBuffer arg1);
    public String getMessage();
    public A withMessage(String message);
    public Boolean hasMessage();
    public A withNewMessage(String arg1);
    public A withNewMessage(StringBuilder arg1);
    public A withNewMessage(StringBuffer arg1);
    public interface VersionRefNested<N> extends io.fabric8.kubernetes.api.builder.Nested<N>,DecisionVersionRefFluent<DecisionRequestStatusFluent.VersionRefNested<N>> {

            public N and();
            public N endVersionRef();    }


}

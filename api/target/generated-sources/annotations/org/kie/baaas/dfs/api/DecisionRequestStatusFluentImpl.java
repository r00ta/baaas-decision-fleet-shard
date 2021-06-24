package org.kie.baaas.dfs.api;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.lang.StringBuilder;
import io.fabric8.kubernetes.api.builder.Nested;
import java.lang.String;
import java.lang.StringBuffer;
import java.lang.Deprecated;
import io.fabric8.kubernetes.api.builder.BaseFluent;
import java.lang.Object;
import io.dekorate.crd.annotation.PrinterColumn;
import java.lang.Boolean;

public class DecisionRequestStatusFluentImpl<A extends DecisionRequestStatusFluent<A>> extends io.fabric8.kubernetes.api.builder.BaseFluent<A> implements DecisionRequestStatusFluent<A> {

    private DecisionVersionRefBuilder versionRef;
    private AdmissionStatus state;
    private String reason;
    private String message;

    public DecisionRequestStatusFluentImpl() {
    }

    public DecisionRequestStatusFluentImpl(DecisionRequestStatus instance) {
        this.withVersionRef(instance.getVersionRef());
        
        this.withState(instance.getState());
        
        this.withReason(instance.getReason());
        
        this.withMessage(instance.getMessage());
    }

    
/**
 * This method has been deprecated, please use method buildVersionRef instead.
 * @return The buildable object.
 */
@Deprecated public DecisionVersionRef getVersionRef() {
        return this.versionRef!=null?this.versionRef.build():null;
    }

    public DecisionVersionRef buildVersionRef() {
        return this.versionRef!=null?this.versionRef.build():null;
    }

    public A withVersionRef(DecisionVersionRef versionRef) {
        _visitables.get("versionRef").remove(this.versionRef);
        if (versionRef!=null){ this.versionRef= new DecisionVersionRefBuilder(versionRef); _visitables.get("versionRef").add(this.versionRef);} return (A) this;
    }

    public Boolean hasVersionRef() {
        return this.versionRef != null;
    }

    public DecisionRequestStatusFluent.VersionRefNested<A> withNewVersionRef() {
        return new VersionRefNestedImpl();
    }

    public DecisionRequestStatusFluent.VersionRefNested<A> withNewVersionRefLike(DecisionVersionRef item) {
        return new VersionRefNestedImpl(item);
    }

    public DecisionRequestStatusFluent.VersionRefNested<A> editVersionRef() {
        return withNewVersionRefLike(getVersionRef());
    }

    public DecisionRequestStatusFluent.VersionRefNested<A> editOrNewVersionRef() {
        return withNewVersionRefLike(getVersionRef() != null ? getVersionRef(): new DecisionVersionRefBuilder().build());
    }

    public DecisionRequestStatusFluent.VersionRefNested<A> editOrNewVersionRefLike(DecisionVersionRef item) {
        return withNewVersionRefLike(getVersionRef() != null ? getVersionRef(): item);
    }

    public AdmissionStatus getState() {
        return this.state;
    }

    public A withState(AdmissionStatus state) {
        this.state=state; return (A) this;
    }

    public Boolean hasState() {
        return this.state != null;
    }

    public String getReason() {
        return this.reason;
    }

    public A withReason(String reason) {
        this.reason=reason; return (A) this;
    }

    public Boolean hasReason() {
        return this.reason != null;
    }

    public A withNewReason(String arg1) {
        return (A)withReason(new String(arg1));
    }

    public A withNewReason(StringBuilder arg1) {
        return (A)withReason(new String(arg1));
    }

    public A withNewReason(StringBuffer arg1) {
        return (A)withReason(new String(arg1));
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

    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DecisionRequestStatusFluentImpl that = (DecisionRequestStatusFluentImpl) o;
        if (versionRef != null ? !versionRef.equals(that.versionRef) :that.versionRef != null) return false;
        if (state != null ? !state.equals(that.state) :that.state != null) return false;
        if (reason != null ? !reason.equals(that.reason) :that.reason != null) return false;
        if (message != null ? !message.equals(that.message) :that.message != null) return false;
        return true;
    }

    public int hashCode() {
        return java.util.Objects.hash(versionRef,  state,  reason,  message,  super.hashCode());
    }

    public class VersionRefNestedImpl<N> extends DecisionVersionRefFluentImpl<DecisionRequestStatusFluent.VersionRefNested<N>> implements DecisionRequestStatusFluent.VersionRefNested<N>,io.fabric8.kubernetes.api.builder.Nested<N> {
        private final DecisionVersionRefBuilder builder;

            VersionRefNestedImpl(DecisionVersionRef item) {
                this.builder = new DecisionVersionRefBuilder(this, item);
                        
            }

            VersionRefNestedImpl() {
                this.builder = new DecisionVersionRefBuilder(this);
                        
            }

            public N and() {
                return (N) DecisionRequestStatusFluentImpl.this.withVersionRef(builder.build());
            }

            public N endVersionRef() {
                return and();
            }
    }


}

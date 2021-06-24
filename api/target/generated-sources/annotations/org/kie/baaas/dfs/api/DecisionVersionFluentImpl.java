package org.kie.baaas.dfs.api;

import java.lang.Object;
import io.fabric8.kubernetes.client.CustomResourceFluentImpl;

public class DecisionVersionFluentImpl<A extends DecisionVersionFluent<A>> extends CustomResourceFluentImpl<DecisionVersionSpec,DecisionVersionStatus,A> implements DecisionVersionFluent<A> {


    public DecisionVersionFluentImpl() {
    }

    public DecisionVersionFluentImpl(DecisionVersion instance) {
        this.withMetadata(instance.getMetadata());
        
        this.withSpec(instance.getSpec());
        
        this.withStatus(instance.getStatus());
        
        this.withKind(instance.getKind());
        
        this.withApiVersion(instance.getApiVersion());
    }

    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        DecisionVersionFluentImpl that = (DecisionVersionFluentImpl) o;
        return true;
    }

    public int hashCode() {
        return java.util.Objects.hash(super.hashCode());
    }

}

package org.kie.baaas.dfs.api;

import java.lang.Object;
import io.fabric8.kubernetes.client.CustomResourceFluentImpl;

public class DecisionRequestFluentImpl<A extends DecisionRequestFluent<A>> extends CustomResourceFluentImpl<DecisionRequestSpec,DecisionRequestStatus,A> implements DecisionRequestFluent<A> {


    public DecisionRequestFluentImpl() {
    }

    public DecisionRequestFluentImpl(DecisionRequest instance) {
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
        DecisionRequestFluentImpl that = (DecisionRequestFluentImpl) o;
        return true;
    }

    public int hashCode() {
        return java.util.Objects.hash(super.hashCode());
    }

}

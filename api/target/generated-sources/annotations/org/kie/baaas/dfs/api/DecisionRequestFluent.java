package org.kie.baaas.dfs.api;

import io.fabric8.kubernetes.client.CustomResourceFluent;

public interface DecisionRequestFluent<A extends DecisionRequestFluent<A>> extends CustomResourceFluent<DecisionRequestSpec,DecisionRequestStatus,A> {


}

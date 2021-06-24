package org.kie.baaas.dfs.api;

import io.fabric8.kubernetes.client.CustomResourceFluent;

public interface DecisionFluent<A extends DecisionFluent<A>> extends CustomResourceFluent<DecisionSpec,DecisionStatus,A> {


}

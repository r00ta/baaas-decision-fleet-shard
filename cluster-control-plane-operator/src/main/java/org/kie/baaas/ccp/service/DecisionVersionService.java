package org.kie.baaas.ccp.service;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClientException;
import org.kie.baaas.api.Decision;
import org.kie.baaas.api.DecisionVersion;
import org.kie.baaas.api.DecisionVersionStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.kie.baaas.ccp.controller.DecisionController.DECISION_LABEL;

@ApplicationScoped
public class DecisionVersionService {

    private static final Logger LOGGER = LoggerFactory.getLogger(DecisionVersionService.class);

    @Inject
    KubernetesClient kubernetesClient;

    @Deprecated
    public DecisionVersion getLatest(String namespace, String decisionName) {
        List<DecisionVersion> revisions = kubernetesClient.customResources(DecisionVersion.class)
                .inNamespace(namespace)
                .withLabel(DECISION_LABEL, decisionName)
                .list()
                .getItems();
        if (!revisions.isEmpty()) {
            Optional<DecisionVersion> latest = revisions.stream().max(Comparator.comparing(a -> a.getSpec().getVersion()));
            if (latest.isPresent()) {
                LOGGER.debug("Fetched latest decision revision {}", decisionName);
                return latest.get();
            }
        }
        LOGGER.debug("Decision Revision not found for {}", decisionName);
        return null;
    }

    @Deprecated
    public void promoteRevision(String namespace, String decisionName) {
        List<DecisionVersion> revisions = kubernetesClient.customResources(DecisionVersion.class)
                .inNamespace(namespace)
                .withLabel(DECISION_LABEL, decisionName)
                .list()
                .getItems();
        if (!revisions.isEmpty()) {
            Optional<DecisionVersion> latest = revisions.stream().filter(a -> a.getMetadata().getDeletionTimestamp() == null).max(Comparator.comparing(a -> a.getSpec().getVersion()));
            if (latest.isPresent()) {
                LOGGER.debug("Promoting decision revision {}", latest.get().getMetadata().getName());
                provision(latest.get());
                Decision decision = kubernetesClient.customResources(Decision.class)
                        .inNamespace(latest.get().getMetadata().getNamespace())
                        .withName(latest.get().getMetadata().getName())
                        .get();
                if (decision != null) {
                    LOGGER.debug("Update revision in decision");
                    decision.getStatus()
                            .setVersionId(latest.get().getSpec().getVersion());
                    kubernetesClient.customResources(Decision.class)
                            .updateStatus(decision);
                }
            }
        }
        LOGGER.debug("No DecisionRevision to promote for decision: {}", decisionName);
    }

    @Deprecated
    public void provision(DecisionVersion revision) {
        if (revision.getStatus() == null) {
            revision.setStatus(new DecisionVersionStatus());
        }
        try {
            //TODO: Do provisioning and register EventSources or Watchers
            revision.setStatus(new DecisionVersionStatus().setBuilding()
                    //with other stuff
            );
            kubernetesClient.customResources(DecisionVersion.class)
                    .inNamespace(revision.getMetadata().getNamespace())
                    .withName(revision.getMetadata().getName())
                    .updateStatus(revision);
        } catch (KubernetesClientException e) {
//            revision.getStatus()
//                    .setPhase(Phase.FAILED)
//                    .getConditions().add(new ConditionBuilder().withMessage(e.getMessage()).withLastTransitionTime(new Date().toString()).with)setMessage(e.getMessage());
            kubernetesClient.customResources(DecisionVersion.class)
                    .inNamespace(revision.getMetadata().getNamespace())
                    .withName(revision.getMetadata().getName())
                    .updateStatus(revision);
        }
    }

}

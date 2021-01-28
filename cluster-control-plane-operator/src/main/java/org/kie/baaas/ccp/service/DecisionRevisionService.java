package org.kie.baaas.ccp.service;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClientException;
import org.kie.baaas.api.Decision;
import org.kie.baaas.api.DecisionRevision;
import org.kie.baaas.api.DecisionRevisionStatus;
import org.kie.baaas.api.DecisionRevisionStatusBuilder;
import org.kie.baaas.api.Phase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.kie.baaas.ccp.controller.DecisionController.DECISION_LABEL;

@ApplicationScoped
public class DecisionRevisionService {

    private static final Logger LOGGER = LoggerFactory.getLogger(DecisionRevisionService.class);

    @Inject
    KubernetesClient kubernetesClient;

    public DecisionRevision getLatest(String namespace, String decisionName) {
        List<DecisionRevision> revisions = kubernetesClient.customResources(DecisionRevision.class)
                .inNamespace(namespace)
                .withLabel(DECISION_LABEL, decisionName)
                .list()
                .getItems();
        if (!revisions.isEmpty()) {
            Optional<DecisionRevision> latest = revisions.stream().max(Comparator.comparingLong(a -> a.getSpec().getId()));
            if (latest.isPresent()) {
                LOGGER.debug("Fetched latest decision revision {}", decisionName);
                return latest.get();
            }
        }
        LOGGER.debug("Decision Revision not found for {}", decisionName);
        return null;
    }

    public void promoteRevision(String namespace, String decisionName) {
        List<DecisionRevision> revisions = kubernetesClient.customResources(DecisionRevision.class)
                .inNamespace(namespace)
                .withLabel(DECISION_LABEL, decisionName)
                .list()
                .getItems();
        if (!revisions.isEmpty()) {
            Optional<DecisionRevision> latest = revisions.stream().filter(a -> a.getMetadata().getDeletionTimestamp() == null).max(Comparator.comparingLong(a -> a.getSpec().getId()));
            if (latest.isPresent()) {
                LOGGER.debug("Promoting decision revision {}", latest.get().getMetadata().getName());
                provision(latest.get());
                Decision decision = kubernetesClient.customResources(Decision.class)
                        .inNamespace(latest.get().getMetadata().getNamespace())
                        .withName(latest.get().getSpec().getDecision())
                        .get();
                if(decision != null) {
                    LOGGER.debug("Update revision in decision");
                    decision.getStatus()
                            .setRevisionId(latest.get().getSpec().getId())
                            .setRevisionName(latest.get().getMetadata().getName());
                    kubernetesClient.customResources(Decision.class)
                            .updateStatus(decision);
                }
            }
        }
        LOGGER.debug("No DecisionRevision to promote for decision: {}", decisionName);
    }

    public void provision(DecisionRevision revision) {
        if (revision.getStatus() == null) {
            revision.setStatus(new DecisionRevisionStatus());
        }
        try {
            //TODO: Do provisioning and register EventSources or Watchers
            revision.setStatus(new DecisionRevisionStatusBuilder()
                    .withPhase(Phase.BUILDING)
                    //with other stuff
                    .build());
            kubernetesClient.customResources(DecisionRevision.class)
                    .inNamespace(revision.getMetadata().getNamespace())
                    .withName(revision.getMetadata().getName())
                    .updateStatus(revision);
        } catch (KubernetesClientException e) {
            revision.getStatus()
                    .setPhase(Phase.FAILED)
                    .setMessage(e.getMessage());
            kubernetesClient.customResources(DecisionRevision.class)
                    .inNamespace(revision.getMetadata().getNamespace())
                    .withName(revision.getMetadata().getName())
                    .updateStatus(revision);
        }
    }
}

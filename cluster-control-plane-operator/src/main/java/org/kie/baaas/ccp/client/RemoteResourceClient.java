/*
 * Copyright 2021 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.kie.baaas.ccp.client;

import java.net.URI;
import javax.enterprise.context.ApplicationScoped;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
import org.kie.baaas.api.Webhook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ApplicationScoped
public class RemoteResourceClient {

    private static final Logger LOGGER = LoggerFactory.getLogger(RemoteResourceClient.class);

    private Client client = ResteasyClientBuilder.newClient();

    public String get(URI path, MediaType mediaType) {
        Response response = client
                .target(path)
                .request(mediaType).get();
        if (response.getStatus() < Response.Status.BAD_REQUEST.getStatusCode()) {
            LOGGER.debug("Successfully fetched remote resource from URI: {} and MediaType: {}", path, mediaType);
            return response.readEntity(String.class);
        }
        LOGGER.warn("Unable to retrieve remote resource from URI: {} and MediaType: {}. Received: {}", path, mediaType, response.getStatus());
        return null;
    }

    public boolean notify(URI path, Webhook event) {
        Response response = client.target(path).request(MediaType.APPLICATION_JSON).post(Entity.json(event));
        if (response.getStatus() < Response.Status.BAD_REQUEST.getStatusCode()) {
            LOGGER.debug("Successfully emitted webhook to URI: {}", path);
            return true;
        }
        LOGGER.warn("Unable to emit webhook to URI: {}. Received: {}", path, response.getStatus());
        return false;
    }
}

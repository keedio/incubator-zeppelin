/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.zeppelin.rest;

import org.apache.zeppelin.server.JsonResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;
import java.io.*;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * API used to retrieve environment information. If development mode is enabled, authentication
 * is automatically performed using a predefined token.
 *
 * Created by Luca Rosellini <lrosellini@keedio.com> on 21/10/15.
 */
@Path("/environment")
@Produces("application/json")
public class PublicRestApi {
    private static final Logger LOG = LoggerFactory.getLogger(PublicRestApi.class);

    private final static String PROP_FILENAME = "application.properties";
    private Boolean devMode = Boolean.FALSE;

    private String token;

    public PublicRestApi() {

        try (Reader r = new InputStreamReader(
            this.getClass().getClassLoader().getResourceAsStream(PROP_FILENAME))){

            Properties props = new Properties();
            props.load(r);

            devMode = Boolean.parseBoolean(props.getProperty("development.mode"));

            String username = props.getProperty("development.username");
            String password = props.getProperty("development.password");
            token = Base64.getEncoder().encodeToString( (username + ":" + password).getBytes() );

        } catch (Exception e) {
            LOG.error("Catched exception, devMode will be set to 'false'",e);

            devMode = Boolean.FALSE;
        }
    }

    /**
     * Get ticket
     * Returns username & ticket
     * for anonymous access, username is always anonymous.
     * After getting this ticket, access through websockets become safe
     *
     * @return 200 response
     */
    @GET
    @Path("ticket")
    public Response ticket() {
        Map<String, String> data = new HashMap<>();
        data.put("isDevelopment", devMode.toString());

        if (devMode)
            data.put("ticket", token);

        return new JsonResponse(Response.Status.OK, "", data).build();
    }

}

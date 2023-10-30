package com.teragrep.snw_01.tableapi;

/*
 * Teragrep ServiceNow Integrator SNW-01
 * Copyright (C) 2023 Suomen Kanuuna Oy
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://github.com/teragrep/teragrep/blob/main/LICENSE>.
 *
 *
 * Additional permission under GNU Affero General Public License version 3
 * section 7
 *
 * If you modify this Program, or any covered work, by linking or combining it
 * with other code, such other code is not for that reason alone subject to any
 * of the requirements of the GNU Affero GPL version 3 as long as this Program
 * is the same Program as licensed from Suomen Kanuuna Oy without any additional
 * modifications.
 *
 * Supplemented terms under GNU Affero General Public License version 3
 * section 7
 *
 * Origin of the software must be attributed to Suomen Kanuuna Oy. Any modified
 * versions must be marked as "Modified version of" The Program.
 *
 * Names of the licensors and authors may not be used for publicity purposes.
 *
 * No rights are granted for use of trade names, trademarks, or service marks
 * which are in The Program if any.
 *
 * Licensee must indemnify licensors and authors for any liability that these
 * contractual assumptions impose on licensors and authors.
 *
 * To the extent this program is licensed as part of the Commercial versions of
 * Teragrep, the applicable Commercial License may apply to this file if you as
 * a licensee so wish it.
 */

import com.teragrep.snw_01.TableDay;
import org.junit.jupiter.api.*;
import org.mockserver.configuration.Configuration;
import org.mockserver.integration.ClientAndServer;
import org.mockserver.model.Parameter;
import org.slf4j.event.Level;

import java.time.Instant;
import java.time.ZoneOffset;

import static org.mockserver.integration.ClientAndServer.startClientAndServer;
import static org.mockserver.model.HttpClassCallback.callback;
import static org.mockserver.model.HttpRequest.request;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class TableApiCallTest {

    private ClientAndServer mockServer;

    @BeforeAll
    public void startServer() {
        Configuration configuration = new Configuration();
        configuration.logLevel(Level.ERROR);
        mockServer = startClientAndServer(configuration, 1080);
    }

    @AfterAll
    public void stopServer() {
        mockServer.stop();
    }

    @Test
    public void testTableApiCall() throws Exception {
        mockServer
                .when(
                        request()
                                .withMethod("GET")
                                .withPath("/api/now/table/syslog_transaction")
                                .withHeader("Accept", "application/json")
                                .withHeader("Authorization", "Basic dXNlcm5hbWU6cGFzc3dvcmQ=")
                                .withHeader("Host", "localhost:1080")
                                .withQueryStringParameter(new Parameter("sysparm_offset"))
                                .withQueryStringParameter(new Parameter("sysparm_limit"))
                                .withQueryStringParameter(new Parameter("sysparm_query"))
                )
                        .respond(
                                callback(APISimulation.class.getCanonicalName())
                        );


        System.setProperty("tableApi.url.scheme", "http");
        System.setProperty("tableApi.url.host", "localhost");
        System.setProperty("tableApi.url.port", "1080");
        System.setProperty("tableApi.username", "username");
        System.setProperty("tableApi.password", "password");


        long recordLimit = 5;
        System.setProperty("tableApi.sysParamLimit", String.valueOf(recordLimit));


        TableApiCall tableApiCall = new TableApiCall(
                new TableDay(Instant.ofEpochSecond(1690502400).atZone(ZoneOffset.UTC), 1, "syslog_transaction")
        );



        APIResponse apiResponse = new APIResponse(tableApiCall.call());

        Assertions.assertEquals(recordLimit, apiResponse.getEvents().size());
    }
}

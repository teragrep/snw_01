package com.teragrep.snw_01;

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

import com.teragrep.rlp_03.FrameProcessor;
import com.teragrep.rlp_03.RelpFrameServerRX;
import com.teragrep.rlp_03.Server;
import com.teragrep.rlp_03.SyslogRXFrameProcessor;
import com.teragrep.snw_01.tableapi.APISimulation;
import org.junit.jupiter.api.*;
import org.mockserver.configuration.Configuration;
import org.mockserver.integration.ClientAndServer;
import org.mockserver.model.Parameter;
import org.slf4j.event.Level;;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static org.mockserver.integration.ClientAndServer.startClientAndServer;
import static org.mockserver.model.HttpClassCallback.callback;
import static org.mockserver.model.HttpRequest.request;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class MainTest {

    private ClientAndServer mockServer;


    private final int relpPort = 3601;
    private final List<byte[]> outputList = new ArrayList<>();
    private final Consumer<RelpFrameServerRX> relpFrameServerRXConsumer = frame -> outputList.add(frame.getData());
    private final Supplier<FrameProcessor> frameProcessorSupplier = () -> new SyslogRXFrameProcessor(relpFrameServerRXConsumer);
    private final Server server = new Server(relpPort, frameProcessorSupplier);


    @BeforeAll
    public void startServer() throws IOException {
        Configuration configuration = new Configuration();
        configuration.logLevel(Level.ERROR);
        mockServer = startClientAndServer(configuration, 2080);
        server.start();
    }

    @AfterAll
    public void stopServer() throws InterruptedException {
        mockServer.stop();
        server.stop();
    }

    @Test
    public void testRun() throws InterruptedException {
        mockServer
                .when(
                        request()
                                .withMethod("GET")
                                .withPath("/api/now/table/sysevent")
                                .withHeader("Accept", "application/json")
                                .withHeader("Authorization", "Basic dXNlcm5hbWU6cGFzc3dvcmQ=")
                                .withHeader("Host", "localhost:2080")
                                .withQueryStringParameter(new Parameter("sysparm_offset"))
                                .withQueryStringParameter(new Parameter("sysparm_limit"))
                                .withQueryStringParameter(new Parameter("sysparm_query"))
                )
                .respond(
                        callback(APISimulation.class.getCanonicalName())
                );

        System.setProperty("statestore.directory", "target");

        System.setProperty("tableApi.url.scheme", "http");
        System.setProperty("tableApi.url.host", "localhost");
        System.setProperty("tableApi.url.port", "2080");
        System.setProperty("tableApi.username", "username");
        System.setProperty("tableApi.password", "password");

        System.setProperty("relp.connection.port", String.valueOf(relpPort));

        long recordLimit = 5;
        System.setProperty("tableApi.sysParamLimit", String.valueOf(recordLimit));

        String[] args = new String[]{};

        Runnable mainClassRunnable = () -> {
            try {
                Main.main(args);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        };

        Thread thread = new Thread(mainClassRunnable);
        thread.start();
        Thread.sleep(1000);
        thread.interrupt();

        Assertions.assertTrue(outputList.size() >= 5);
    }
}

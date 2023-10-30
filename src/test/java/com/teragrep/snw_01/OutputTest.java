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

import com.codahale.metrics.MetricRegistry;
import com.teragrep.rlp_03.FrameProcessor;
import com.teragrep.rlp_03.RelpFrameServerRX;
import com.teragrep.rlp_03.Server;
import com.teragrep.rlp_03.SyslogRXFrameProcessor;
import org.junit.jupiter.api.*;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class OutputTest {

    private final int relpPort = 2601;
    private final List<byte[]> outputList = new ArrayList<>();

    private final Consumer<RelpFrameServerRX> relpFrameServerRXConsumer = frame -> outputList.add(frame.getData());
    private final Supplier<FrameProcessor> frameProcessorSupplier = () -> new SyslogRXFrameProcessor(relpFrameServerRXConsumer);
    private final Server server = new Server(relpPort, frameProcessorSupplier);

    @BeforeAll
    public void startRelpServer() throws IOException {
        server.start();
    }

    @AfterAll
    public void stopRelpServer() throws InterruptedException {
        server.stop();
    }

    @Test
    public void testOutput() {
        String input = "test";
        byte[] inputBytes = input.getBytes(StandardCharsets.UTF_8);
        try (
                Output output = new Output(
                        "relpPutput",
                        "localhost",
                        relpPort,
                        500,
                        2000,
                        2500,
                        500,
                        new MetricRegistry()
                )
        ) {
            output.accept(inputBytes);
        }

        Assertions.assertEquals(input, new String(outputList.get(0), StandardCharsets.UTF_8));
    }
}

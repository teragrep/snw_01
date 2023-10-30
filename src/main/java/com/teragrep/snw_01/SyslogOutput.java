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

import com.teragrep.rlo_14.Facility;
import com.teragrep.rlo_14.SDElement;
import com.teragrep.rlo_14.Severity;
import com.teragrep.rlo_14.SyslogMessage;
import com.teragrep.snw_01.config.RelpConfig;
import com.teragrep.snw_01.config.SyslogConfig;
import com.teragrep.snw_01.tableapi.EventMetadata;
import jakarta.json.JsonObject;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.time.ZonedDateTime;
import java.util.function.Consumer;

public class SyslogOutput implements Consumer<JsonObject> {

    private final String hostname;
    private final String appName;
    private final Output output;
    private final String realHostname;

    final TableDay tableDay;

    SyslogOutput(TableDay tableDay) {
        this.tableDay = tableDay;
        SyslogConfig syslogConfig = new SyslogConfig();
        this.hostname = syslogConfig.getSyslogHostname();
        this.appName = syslogConfig.getSyslogAppName();

        RelpConfig relpConfig = new RelpConfig();

        this.output = new Output(
                "Output",
                relpConfig.getRelpAddress(),
                relpConfig.getRelpPort(),
                relpConfig.getConnectTimeout(),
                relpConfig.getReadTimeout(),
                relpConfig.getWriteTimeout(),
                500,
                tableDay.metricRegistry
        );

        String realHost;
        try {
            realHost = InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
            realHost = "localhost";
        }

        this.realHostname = realHost;
    }



    @Override
    public void accept(JsonObject jsonObject) {

        final EventMetadata eventMetadata = new EventMetadata(jsonObject);

        final SDElement origin_48577 = origin48577();

        final SDElement event_id_48577 = eventId48577(eventMetadata);

        final SDElement snw_01_48577 = snw_01_48577(eventMetadata);

        SyslogMessage syslogMessage = new SyslogMessage();
        syslogMessage = syslogMessage
                .withHostname(hostname)
                .withAppName(appName)
                .withTimestamp(eventMetadata.getSysCreatedOn().toInstant()) // epoch millis
                .withSeverity(Severity.WARNING)
                .withFacility(Facility.USER)
                .withMsg(jsonObject.toString())
                .withSDElement(event_id_48577)
                .withSDElement(origin_48577)
                .withSDElement(snw_01_48577);


        output.accept(syslogMessage.toRfc5424SyslogMessage().getBytes(StandardCharsets.UTF_8));
    }


    private SDElement eventId48577(EventMetadata eventMetadata) {

        final SDElement event_id_48577 = new SDElement("event_id@48577");
        event_id_48577.addSDParam("hostname", realHostname);
        String uuid = eventMetadata.getSysId();

        event_id_48577.addSDParam("uuid", uuid);
        event_id_48577.addSDParam("source", "source");
        ZonedDateTime timestamp = eventMetadata.getSysCreatedOn();
        event_id_48577.addSDParam("unixtime", String.valueOf(timestamp.toInstant().getEpochSecond()));
        return event_id_48577;


    }

    private SDElement origin48577() {
        final SDElement origin_48577 = new SDElement("origin@48577");
        origin_48577.addSDParam("hostname", realHostname);
        return origin_48577;

    }


    private SDElement snw_01_48577(EventMetadata eventMetadata) {

        final SDElement snw_01 = new SDElement("snw_01@48577");

        snw_01.addSDParam("tableName", tableDay.table);
        snw_01.addSDParam("sys_id", eventMetadata.getSysId());

        return snw_01;

    }
}

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

import jakarta.json.Json;
import jakarta.json.JsonArrayBuilder;
import jakarta.json.JsonObject;
import jakarta.json.JsonObjectBuilder;
import org.mockserver.mock.action.ExpectationResponseCallback;
import org.mockserver.model.HttpRequest;
import org.mockserver.model.HttpResponse;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.mockserver.model.HttpResponse.response;


public class APISimulation implements ExpectationResponseCallback {

    /**
     * contains EventSequences for configurable amount of days
     */
    public APISimulation() {

    }


    private JsonObject toJson(List<EventMetadata> eventMetadata) {
        JsonObjectBuilder jsonObjectBuilder = Json.createObjectBuilder();

        JsonArrayBuilder jsonArrayBuilder = Json.createArrayBuilder();
        for (EventMetadata e : eventMetadata) {
            jsonArrayBuilder.add(e.toJson());
        }

        jsonObjectBuilder.add("result", jsonArrayBuilder);


        return jsonObjectBuilder.build();
    }

    private List<EventMetadata> createResponseEvents(ZonedDateTime start, ZonedDateTime end, long offset, long limit) {

        Duration frequency = Duration.of(5, ChronoUnit.SECONDS);

        EventSequence eventSequence = new EventSequence(
                start.toInstant(),
                frequency
        );

        long maxStreamLength = (long) Math.ceil(
                ((double) end.toEpochSecond() - (double) start.toEpochSecond())
                        / (double) frequency.toSeconds()
        );

        return Stream.generate(eventSequence)
                .limit(maxStreamLength)
                .filter(
                        (eventMetadata) -> eventMetadata.getSysCreatedOn().isAfter(start)
                                && eventMetadata.getSysCreatedOn().isBefore(end)
                                || eventMetadata.getSysCreatedOn().isEqual(start)
                                || eventMetadata.getSysCreatedOn().isEqual(end)
                )
                .skip(offset)
                .limit(limit)
                .collect(Collectors.toList());
    }

    @Override
    public HttpResponse handle(HttpRequest httpRequest) throws Exception {
        String sysParamOffsetString = httpRequest.getFirstQueryStringParameter("sysparm_offset");
        String sysParamLimitString = httpRequest.getFirstQueryStringParameter("sysparm_limit");
        String sysParamQueryString = httpRequest.getFirstQueryStringParameter("sysparm_query");

        long sysParamOffset = Long.parseLong(sysParamOffsetString);
        long sysParamLimit = Long.parseLong(sysParamLimitString);
        SysParamQuery sysParamQuery = new SysParamQuery(sysParamQueryString);

        final List<EventMetadata> eventMetadata = createResponseEvents(
                sysParamQuery.getStart(),
                sysParamQuery.getEnd(),
                sysParamOffset,
                sysParamLimit
        );

        assert eventMetadata.size() <= sysParamLimit;

        final JsonObject responseJson = toJson(eventMetadata);

        return response().withBody(responseJson.toString());
    }
}

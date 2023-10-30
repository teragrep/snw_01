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
import jakarta.json.JsonObject;
import jakarta.json.JsonObjectBuilder;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public final class EventMetadata {

    final Map<String, String> contents = new HashMap<>();

    /**
     * For ApiSimulation only
     * @return sys_created_on
     */
    EventMetadata(String sysUpdatedOn, String sysCreatedOn, String closedAt, String openedAt, String sysId) {
        contents.put("sys_updated_on", sysUpdatedOn);
        contents.put("sys_created_on", sysCreatedOn);
        contents.put("closed_at", closedAt);
        contents.put("opened_at", openedAt);
        contents.put("sys_id", sysId);
    }

    public EventMetadata(JsonObject eventJson) {
        String sysCreatedOn = eventJson.getString("sys_created_on");
        String sysId = eventJson.getString("sys_id");

        contents.put("sys_created_on", sysCreatedOn);
        contents.put("sys_id", sysId);
    }

    /**
     * For ApiSimulation only
     * @return sys_created_on
     */
    public ZonedDateTime getSysCreatedOn () {
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH);

        LocalDateTime ldt = LocalDateTime.parse(contents.get("sys_created_on"), dateTimeFormatter);
        return ZonedDateTime.of(ldt, ZoneOffset.UTC);
    }

    public String getSysId() {
        return contents.get("sys_id");
    }

    /**
     * For ApiSimulation only
     * @return contents as JsonObject
     */
    JsonObject toJson() {
        JsonObjectBuilder jsonObjectBuilder = Json.createObjectBuilder();
        for (Map.Entry<String, String> entry : contents.entrySet()) {
            jsonObjectBuilder.add(entry.getKey(), entry.getValue());
        }

        return jsonObjectBuilder.build();
    }
}

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

import com.teragrep.snw_01.config.StateStoreConfig;
import jakarta.json.*;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.HashMap;

class TableOffset {

    private long offset;
    final File file;
    final TableDay tableDay;

    TableOffset(TableDay tableDay) {
        this.tableDay = tableDay;
        StateStoreConfig stateStoreConfig = new StateStoreConfig();
        String stateDir = stateStoreConfig.getStateStoreDirectory();
        this.file = Paths.get(stateDir + "/" + this.tableDay.table + "_" + this.tableDay.startOf.toEpochSecond() + ".json").toFile();
        this.offset = deserializeOffset();
    }

    private long deserializeOffset() {
        long rv;
        try (FileInputStream fis = new FileInputStream(file)) {
            JsonReaderFactory jsonReaderFactory = Json.createReaderFactory(new HashMap<>());

            try (JsonReader jsonReader = jsonReaderFactory.createReader(fis, StandardCharsets.UTF_8)) {

                JsonObject offsetObject = jsonReader.readObject();

                if (!offsetObject.isNull("offset")) {
                    rv = offsetObject.getJsonNumber("offset").longValue();
                } else {
                    throw new IllegalArgumentException("invalid json object in file <[" + file.getAbsolutePath() + "]>");
                }
            }
        }
        catch (FileNotFoundException fileNotFoundException) {
            rv = 0;
        }
        catch (IOException ioException) {
            throw new UncheckedIOException(ioException);
        }

        return rv;
    }
    private void serializeOffset() {
        JsonObjectBuilder jsonObjectBuilder = Json.createObjectBuilder();
        jsonObjectBuilder.add("offset", offset);
        JsonObject jsonObject = jsonObjectBuilder.build();

        try (FileOutputStream fos = new FileOutputStream(file)) {
            JsonWriterFactory jsonWriterFactory = Json.createWriterFactory(new HashMap<>());
            try (JsonWriter jsonWriter = jsonWriterFactory.createWriter(fos, StandardCharsets.UTF_8)) {
                jsonWriter.write(jsonObject);
            }
        } catch (IOException ioException) {
            throw new UncheckedIOException(ioException);
        }
    }


    public void increment(long numberOfRecords) {
        offset = offset + numberOfRecords;
        serializeOffset();
    }

    public long getOffset() {
        return offset;
    }
}

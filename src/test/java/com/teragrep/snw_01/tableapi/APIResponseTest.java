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

import jakarta.json.JsonObject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;

public class APIResponseTest {

    @Test
    public void testAPIResponse() {
        String response = "{\"result\":[{\"opened_at\":\"2023-07-28 00:00:00\",\"sys_id\":\"04366384-f5e1-413d-9a98-d75c3e565c05\",\"closed_at\":\"2023-07-28 00:00:00\",\"sys_created_on\":\"2023-07-28 00:00:00\",\"sys_updated_on\":\"2023-07-28 00:00:00\",\"sys_domain\":{\"another key\":\"an another value\",\"key\":\"a value\"}},{\"opened_at\":\"2023-07-28 00:00:05\",\"sys_id\":\"4f69d272-2f0e-46e7-af29-ad4fd153bc5a\",\"closed_at\":\"2023-07-28 00:00:05\",\"sys_created_on\":\"2023-07-28 00:00:05\",\"sys_updated_on\":\"2023-07-28 00:00:05\",\"sys_domain\":{\"another key\":\"an another value\",\"key\":\"a value\"}},{\"opened_at\":\"2023-07-28 00:00:10\",\"sys_id\":\"def9786b-1b13-4c5f-83da-ac729365cd3b\",\"closed_at\":\"2023-07-28 00:00:10\",\"sys_created_on\":\"2023-07-28 00:00:10\",\"sys_updated_on\":\"2023-07-28 00:00:10\",\"sys_domain\":{\"another key\":\"an another value\",\"key\":\"a value\"}},{\"opened_at\":\"2023-07-28 00:00:15\",\"sys_id\":\"259ebadf-6668-4030-b054-63609cce2bab\",\"closed_at\":\"2023-07-28 00:00:15\",\"sys_created_on\":\"2023-07-28 00:00:15\",\"sys_updated_on\":\"2023-07-28 00:00:15\",\"sys_domain\":{\"another key\":\"an another value\",\"key\":\"a value\"}},{\"opened_at\":\"2023-07-28 00:00:20\",\"sys_id\":\"d6168558-3e60-4f76-98fc-f6c2cf6ecb57\",\"closed_at\":\"2023-07-28 00:00:20\",\"sys_created_on\":\"2023-07-28 00:00:20\",\"sys_updated_on\":\"2023-07-28 00:00:20\",\"sys_domain\":{\"another key\":\"an another value\",\"key\":\"a value\"}}]}";
        APIResponse apiResponse = new APIResponse(response);

        List<JsonObject> apiEventMetadata = apiResponse.getEvents();

        Assertions.assertEquals(5, apiEventMetadata.size());
    }
}

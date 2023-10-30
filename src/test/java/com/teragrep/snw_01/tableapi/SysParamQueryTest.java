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

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;

public class SysParamQueryTest {

    @Test
    public void testQueryParse() {
        SysParamQuery sysParamQuery = new SysParamQuery("sys_created_on>=2023-07-27 00:00:00^sys_created_on<=2023-07-27 23:59:59");

        Instant startInstant = Instant.ofEpochSecond(1690416000);
        Assertions.assertEquals(startInstant,sysParamQuery.getStart().toInstant());

        Instant endInstant = Instant.ofEpochSecond(1690502399);
        Assertions.assertEquals(endInstant,sysParamQuery.getEnd().toInstant());
    }

    @Test
    public void testQueryProduce() {
        Instant startInstant = Instant.ofEpochSecond(1690416000);
        ZonedDateTime startZDT = startInstant.atZone(ZoneOffset.UTC);

        Instant endInstant = Instant.ofEpochSecond(1690502399);
        ZonedDateTime endZDT = endInstant.atZone(ZoneOffset.UTC);

        SysParamQuery sysParamQuery = new SysParamQuery(startZDT, endZDT);
        Assertions.assertEquals("sys_created_on>=2023-07-27 00:00:00^sys_created_on<=2023-07-27 23:59:59", sysParamQuery.query);
    }
}

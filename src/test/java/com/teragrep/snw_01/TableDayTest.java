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

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;

public class TableDayTest {

    @Test
    public void testDayValid() {
        Instant instant = Instant.now();
        ZonedDateTime startOfDay = ZonedDateTime
                .ofInstant(instant, ZoneOffset.UTC)
                .toLocalDate()
                .atStartOfDay(ZoneOffset.UTC);

        TableDay tableDay = new TableDay(startOfDay, 0, "tablish");

        Assertions.assertFalse(tableDay.isExpired());
    }

    @Test
    public void testDayExpire() {
        Instant instant = Instant.now().minusSeconds(24*3600*2);
        ZonedDateTime startOfDay = ZonedDateTime
                .ofInstant(instant, ZoneOffset.UTC)
                .toLocalDate()
                .atStartOfDay(ZoneOffset.UTC);

        TableDay tableDay = new TableDay(startOfDay, 1, "someOtherTable");

        Assertions.assertTrue(tableDay.isExpired());
    }

    @Test
    public void testDayEnd() {
        ZonedDateTime startOf = Instant.ofEpochSecond(1690502400).atZone(ZoneOffset.UTC);
        TableDay tableDay = new TableDay(startOf, 1, "someTable");

        ZonedDateTime endOf = Instant.ofEpochSecond(1690502400+24*3600-1).atZone(ZoneOffset.UTC);
        Assertions.assertEquals(endOf, tableDay.getEndOf());
    }

    @Test
    public void testEquality() {
        ZonedDateTime startOf = Instant.ofEpochSecond(1690502400).atZone(ZoneOffset.UTC);
        TableDay tableDay = new TableDay(startOf, 1, "someTable");
        TableDay otherDay = new TableDay(startOf, 1, "someTable");

        Assertions.assertEquals(tableDay, otherDay);

    }

    @Test
    public void testEqualityDifferentExpiration() {
        ZonedDateTime startOf = Instant.ofEpochSecond(1690502400).atZone(ZoneOffset.UTC);
        TableDay tableDay = new TableDay(startOf, 1, "someTable");
        TableDay otherDay = new TableDay(startOf, 2, "someTable");

        Assertions.assertEquals(tableDay, otherDay);
    }

    @Test
    void testNotEquals() {
        ZonedDateTime startOf = Instant.ofEpochSecond(1690502400).atZone(ZoneOffset.UTC);
        TableDay tableDay = new TableDay(startOf, 1, "someTable");
        TableDay otherDay = new TableDay(startOf, 1, "notTheSame");

        Assertions.assertNotEquals(tableDay, otherDay);
    }

    @Test
    public void testNotEqualsStartOf() {
        ZonedDateTime startOf = Instant.ofEpochSecond(1690502400).atZone(ZoneOffset.UTC);
        ZonedDateTime otherStartOf = Instant.ofEpochSecond(1690502401).atZone(ZoneOffset.UTC);
        TableDay tableDay = new TableDay(startOf, 1, "someTable");
        TableDay otherDay = new TableDay(otherStartOf, 1, "someTable");

        Assertions.assertNotEquals(tableDay, otherDay);

    }


}

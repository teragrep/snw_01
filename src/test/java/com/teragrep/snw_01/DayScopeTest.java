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

import java.util.List;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.TimeZone;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

public class DayScopeTest {

    @Test
    public void testDayScope() {
        long numberOfDays = 5;

        List<String> expectedTables = new ArrayList<>();
        expectedTables.add("sysevent");
        expectedTables.add("syslog_transaction");

        List<Long> dayStartEpochs = new ArrayList<>();

        // bad old calendar, for testing reference
        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        for (int i = 0; i < numberOfDays; i++ ) {
            calendar.set(Calendar.HOUR_OF_DAY, 0);
            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.SECOND, 0);
            calendar.set(Calendar.MILLISECOND,0);

            dayStartEpochs.add(calendar.getTime().getTime());

            calendar.add(Calendar.DAY_OF_YEAR, -1);
        }

        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(2);
        DayScope dayScope = new DayScope(numberOfDays, scheduler);

        for (TableDay tableDay : dayScope.getDays()) {
            // ensure this day has an expected start
            Assertions.assertTrue(dayStartEpochs.contains(tableDay.startOf.toInstant().toEpochMilli()));
            // ensure this table is an expected one
            Assertions.assertTrue(expectedTables.contains(tableDay.table));
        }


    }
}

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
import com.teragrep.snw_01.config.TableApiRequestConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.concurrent.ScheduledExecutorService;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class DayScope implements Runnable {

    private static final Logger LOGGER = LoggerFactory.getLogger(DayScope.class);

    Collection<TableDay> tableDays = new HashSet<>();

    final long numberOfDays;

    final ScheduledExecutorService scheduler;

    final MetricRegistry metricRegistry;

    public DayScope(long numberOfDays, ScheduledExecutorService scheduler, MetricRegistry metricRegistry) {
        this.numberOfDays = numberOfDays;
        this.scheduler = scheduler;
        this.metricRegistry = metricRegistry;
    }

    // for testing
    public DayScope(long numberOfDays, ScheduledExecutorService scheduler) {
        this.numberOfDays = numberOfDays;
        this.scheduler = scheduler;
        this.metricRegistry = new MetricRegistry();
    }

    List<TableDay> getDays() {

        TableApiRequestConfig tableApiRequestConfig = new TableApiRequestConfig();
        List<String> tables = tableApiRequestConfig.getTableNames();

        LOGGER.debug("configured monitoring for tables <[{}]>", tables);

        List<TableDay> tableDayList = new ArrayList<>();
        for (String table : tables) {
            Supplier<TableDay> decendingDaySupplier = new Supplier<>() {

                ZonedDateTime startOfDay = ZonedDateTime
                        .now(ZoneOffset.UTC)
                        .toLocalDate()
                        .atStartOfDay(ZoneOffset.UTC);

                @Override
                public TableDay get() {
                    ZonedDateTime currentDay = startOfDay;
                    startOfDay = startOfDay.minusDays(1);

                    return new TableDay(currentDay, numberOfDays, table, metricRegistry);
                }
            };

            tableDayList.addAll(Stream.generate(decendingDaySupplier).limit(numberOfDays).collect(Collectors.toList()));
        }
        return tableDayList;
    }

    @Override
    public void run() {
        List<TableDay> relevantTableDays = getDays();

        // remove already present days
        relevantTableDays.removeAll(tableDays);

        if (relevantTableDays.size() == 0) {
            LOGGER.debug("no relevantTableDays <{}> to add to scheduler", relevantTableDays);
        }
        else {
            LOGGER.info("adding relevantTableDays <{}> to scheduler", relevantTableDays);
        }

        for (TableDay tableDay : relevantTableDays) {
            tableDay.schedule(scheduler);
            tableDays.add(tableDay);
        }
    }
}

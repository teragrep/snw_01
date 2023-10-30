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

import com.codahale.metrics.Counter;
import com.codahale.metrics.MetricRegistry;
import com.teragrep.snw_01.config.SchedulerConfig;
import com.teragrep.snw_01.config.TableApiRequestConfig;
import com.teragrep.snw_01.tableapi.APIResponse;
import com.teragrep.snw_01.tableapi.TableApiCall;
import jakarta.json.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Objects;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import static com.codahale.metrics.MetricRegistry.name;

public class TableDay implements Runnable {

    private static final Logger LOGGER = LoggerFactory.getLogger(TableDay.class);

    public final ZonedDateTime startOf;
    public final long expireAfterDays;
    public final String table;
    private ScheduledFuture<?> scheduledFuture;
    final TableOffset tableOffset;

    private final SyslogOutput syslogOutput;

    private final TableApiCall tableApiCall;

    public final MetricRegistry metricRegistry;

    private final Counter records;

    private final Counter errors;

    private final int pollInterval;

    private final long fetchSize;

    public TableDay(ZonedDateTime startOf, long expireAfterDays, String table, MetricRegistry metricRegistry) {
        this.startOf = startOf;
        this.expireAfterDays = expireAfterDays;
        this.table = table;
        this.metricRegistry = metricRegistry;
        this.tableOffset = new TableOffset(this);
        this.syslogOutput = new SyslogOutput(this);
        this.tableApiCall = new TableApiCall(this);

        this.records = metricRegistry.counter(name(TableDay.class, "<[" + table + "]><"+startOf.toEpochSecond()+">", "records"));
        this.errors = metricRegistry.counter(name(TableDay.class,"errors"));

        SchedulerConfig schedulerConfig = new SchedulerConfig();
        this.pollInterval = schedulerConfig.getSchedulerInterval();

        TableApiRequestConfig tableApiRequestConfig = new TableApiRequestConfig();
        this.fetchSize = tableApiRequestConfig.getSysparmLimit();
    }

    // for testing
    public TableDay(ZonedDateTime startOf, long expireAfterDays, String table) {
        this(startOf, expireAfterDays, table, new MetricRegistry());
    }

    public ZonedDateTime getEndOf() {
        return startOf.plusDays(1).minusSeconds(1);

    }

    public boolean isExpired() {
        ZonedDateTime expiryTime = startOf.plusDays(expireAfterDays);

        ZonedDateTime timeNow = ZonedDateTime
                .now(ZoneOffset.UTC)
                .toLocalDate()
                .atStartOfDay(ZoneOffset.UTC);

        return expiryTime.isBefore(timeNow);
    }


    public void schedule(ScheduledExecutorService scheduler) {
         scheduledFuture = scheduler.scheduleWithFixedDelay(
                this,
                0,
                pollInterval,
                TimeUnit.MILLISECONDS

        );
    }

    @Override
    public void run() {
        if (this.isExpired()) {
            LOGGER.info("Stopping monitoring of <{}>", this);
            scheduledFuture.cancel(false);
            return;
        }



        LOGGER.debug("Requesting data for tableDay <{}>", this);
        try {

            boolean fetchMore = true;
            while (fetchMore) {
                String apiCallResult = tableApiCall.call();
                APIResponse apiResponse = new APIResponse(apiCallResult);

                long numberOfRecords = 0;
                for (JsonObject eventJsonObject : apiResponse.getEvents()) {
                    syslogOutput.accept(eventJsonObject);
                    numberOfRecords++;
                }

                LOGGER.debug("Found numberOfRecords <{}> for tableDay <{}>", numberOfRecords, this);
                tableOffset.increment(numberOfRecords);

                records.inc(numberOfRecords);

                if (numberOfRecords < fetchSize) {
                    fetchMore = false;
                }
            }
        }
        catch (Exception e) {
            errors.inc();
            LOGGER.error("exception <{}> while processing tableDay <{}> will retry", e.getMessage(), this, e);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TableDay tableDay = (TableDay) o;
        return Objects.equals(startOf, tableDay.startOf) && Objects.equals(table, tableDay.table);
    }

    @Override
    public int hashCode() {
        return Objects.hash(startOf, table);
    }

    @Override
    public String toString() {
        return "TableDay{" +
                "startOf=" + startOf +
                ", expireAfterDays=" + expireAfterDays +
                ", table='" + table + '\'' +
                '}';
    }

    public long getOffset() {
        return tableOffset.getOffset();
    }
}

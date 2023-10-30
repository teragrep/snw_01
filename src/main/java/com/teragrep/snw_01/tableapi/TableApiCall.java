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

import com.codahale.metrics.Timer;
import com.teragrep.snw_01.Output;
import com.teragrep.snw_01.TableDay;
import com.teragrep.snw_01.config.TableApiConfig;
import com.teragrep.snw_01.config.TableApiRequestConfig;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.impl.classic.BasicHttpClientResponseHandler;
import org.apache.hc.core5.http.NameValuePair;
import org.apache.hc.core5.http.io.HttpClientResponseHandler;
import org.apache.hc.core5.http.message.BasicNameValuePair;
import org.apache.hc.core5.net.URIBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.URISyntaxException;

import static com.codahale.metrics.MetricRegistry.name;

public class TableApiCall {

    private static final Logger LOGGER = LoggerFactory.getLogger(TableApiCall.class);

    // sysparm_offset=sysparm_offset+sysparm_limit

    /*
        curl "https://instance.servicenow.com/api/now/table/problem?sysparm_limit=1" \
            --request GET \
            --header "Accept:application/json" \
            --user 'username':'password'
     */

    final TableDay tableDay;
    final TableApiConfig tableApiConfig = new TableApiConfig();
    final TableApiRequestConfig tableApiRequestConfig = new TableApiRequestConfig();

    private final Timer callLatency;

    public TableApiCall(TableDay tableDay) {
        this.tableDay = tableDay;

        this.callLatency = tableDay.metricRegistry.timer(name(TableApiCall.class, "apiLatency"));
    }

    public URI makeURI() throws URISyntaxException {
        URIBuilder uriBuilder = new URIBuilder();
        uriBuilder.setScheme(tableApiConfig.getResourceUrlScheme());
        uriBuilder.setHost(tableApiConfig.getResourceUrlHost());
        uriBuilder.setPort(tableApiConfig.getResourceUrlPort());

        String path = "/api/now/table/" + tableDay.table;

        uriBuilder.setPath(path);

        // offset
        NameValuePair sysParamOffset = new BasicNameValuePair("sysparm_offset", String.valueOf(tableDay.getOffset()));
        uriBuilder.addParameter(sysParamOffset);

        // limit
        NameValuePair sysParamLimit = new BasicNameValuePair( "sysparm_limit", String.valueOf(tableApiRequestConfig.getSysparmLimit()));
        uriBuilder.addParameter(sysParamLimit);

        // query
        SysParamQuery sysParamQuery = new SysParamQuery(tableDay.startOf, tableDay.getEndOf());
        NameValuePair query = new BasicNameValuePair("sysparm_query", sysParamQuery.query);
        uriBuilder.addParameter(query);

        return uriBuilder.build();
    }

    public String call() throws Exception {
        try (final Timer.Context context = callLatency.time()) {
            LOGGER.debug("Attempting call for tableDay <{}>", tableDay);
            try (
                    TableApiHttpClient tableApiHttpClient = new TableApiHttpClient()
            ) {
                HttpGet httpGet = new HttpGet(makeURI());
                httpGet.addHeader("Accept", "application/json");

                HttpClientResponseHandler<String> basicHttpClientResponseHandler = new BasicHttpClientResponseHandler();

                String rv = tableApiHttpClient.execute(httpGet, basicHttpClientResponseHandler);
                LOGGER.trace("Call for tableDay <{}> return value <[{}]>", tableDay, rv);
                return rv;
            }
        }
    }
}

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

import com.teragrep.snw_01.config.ProxyConfig;
import com.teragrep.snw_01.config.TableApiConfig;
import org.apache.hc.client5.http.auth.AuthScope;
import org.apache.hc.client5.http.auth.UsernamePasswordCredentials;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.impl.auth.BasicCredentialsProvider;
import org.apache.hc.client5.http.impl.auth.BasicScheme;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.routing.DefaultProxyRoutePlanner;
import org.apache.hc.client5.http.protocol.HttpClientContext;
import org.apache.hc.core5.http.HttpHost;
import org.apache.hc.core5.http.io.HttpClientResponseHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class TableApiHttpClient implements AutoCloseable {

    private static final Logger LOGGER = LoggerFactory.getLogger(TableApiHttpClient.class);

    // https://www.baeldung.com/httpclient-advanced-config

    final TableApiConfig tableApiConfig = new TableApiConfig();
    final ProxyConfig proxyConfig = new ProxyConfig();

    final CloseableHttpClient httpclient;

    final HttpClientContext httpClientContext;

    final HttpHost target;

    TableApiHttpClient() {
        BasicCredentialsProvider credsProvider = new BasicCredentialsProvider();


        target = new HttpHost(
                tableApiConfig.getResourceUrlScheme(),
                tableApiConfig.getResourceUrlHost(),
                tableApiConfig.getResourceUrlPort()
        );

        credsProvider.setCredentials(
                new AuthScope(target),
                new UsernamePasswordCredentials(
                        tableApiConfig.getApiUsername(),
                        tableApiConfig.getApiPassword().toCharArray()
                )
        );

        LOGGER.debug("proxyConfig.isProxyEnabled() <{}>", proxyConfig.isProxyEnabled());

        if (proxyConfig.isProxyEnabled()) {

            HttpHost proxy = new HttpHost(
                    proxyConfig.getProxyHost(),
                    proxyConfig.getProxyPort()
            );

            LOGGER.debug("proxyConfig.isAuthenticationEnabled() <{}>", proxyConfig.isAuthenticationEnabled());
            if (proxyConfig.isAuthenticationEnabled()) {

                credsProvider.setCredentials(
                        new AuthScope(proxy),
                        new UsernamePasswordCredentials(
                                proxyConfig.getProxyUsername(),
                                proxyConfig.getProxyPassword().toCharArray()
                        )
                );
            }

            DefaultProxyRoutePlanner routePlanner = new DefaultProxyRoutePlanner(proxy);

            this.httpclient = HttpClients
                    .custom()
                    .setDefaultCredentialsProvider(credsProvider)
                    .setRoutePlanner(routePlanner)
                    .build();
        }
        else {
            this.httpclient = HttpClients
                    .custom()
                    .setDefaultCredentialsProvider(credsProvider)
                    .build();
        }

        // api uses pre-emptive auth, well fortunately it is supported
        final BasicScheme basicAuth = new BasicScheme();
        basicAuth.initPreemptive(
                new UsernamePasswordCredentials(
                        tableApiConfig.getApiUsername(),
                        tableApiConfig.getApiPassword().toCharArray()
                )

        );

        this.httpClientContext = HttpClientContext.create();
        this.httpClientContext.setCredentialsProvider(credsProvider);

        this.httpClientContext.resetAuthExchange(
                target,
                basicAuth
        );
    }

    @Override
    public void close() throws Exception {
        LOGGER.debug("close()");
        httpclient.close();
    }


    public String execute(HttpGet httpGet, HttpClientResponseHandler<String> basicHttpClientResponseHandler) throws IOException {
        LOGGER.debug("execute() httpGet <[{}]>", httpGet);
        String rv = httpclient.execute( httpGet, httpClientContext, basicHttpClientResponseHandler);
        LOGGER.trace("execute() httpGet <[{}]> return value <[{}]>", httpGet, rv);
        return rv;
    }
}

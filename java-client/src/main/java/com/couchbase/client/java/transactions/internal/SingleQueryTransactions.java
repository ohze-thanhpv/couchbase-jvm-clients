/*
 * Copyright 2022 Couchbase, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.couchbase.client.java.transactions.internal;

import com.couchbase.client.core.Core;
import com.couchbase.client.core.annotation.Stability;
import com.couchbase.client.core.deps.com.fasterxml.jackson.databind.node.ObjectNode;
import com.couchbase.client.core.error.EncodingFailureException;
import com.couchbase.client.core.json.Mapper;
import com.couchbase.client.core.transaction.CoreTransactionsReactive;
import com.couchbase.client.core.transaction.config.CoreSingleQueryTransactionOptions;
import com.couchbase.client.core.transaction.config.CoreTransactionsConfig;
import com.couchbase.client.java.codec.JsonSerializer;
import com.couchbase.client.java.env.ClusterEnvironment;
import com.couchbase.client.java.json.JsonObject;
import com.couchbase.client.java.query.QueryOptions;
import com.couchbase.client.java.query.QueryResult;
import com.couchbase.client.java.query.ReactiveQueryResult;
import reactor.core.publisher.Mono;
import reactor.util.annotation.Nullable;

import java.io.IOException;
import java.util.Optional;
import java.util.function.Consumer;

import static com.couchbase.client.core.transaction.config.CoreTransactionsConfig.DEFAULT_TRANSACTION_DURABILITY_LEVEL;
import static com.couchbase.client.core.transaction.config.CoreTransactionsConfig.DEFAULT_TRANSACTION_TIMEOUT;

@Stability.Internal
public class SingleQueryTransactions {
    public static Mono<QueryResult> singleQueryTransactionBuffered(Core core,
                                                                   ClusterEnvironment environment,
                                                                   String statement,
                                                                   @Nullable String bucketName,
                                                                   @Nullable String scopeName,
                                                                   QueryOptions.Built opts) {
        if (opts.retryStrategy().isPresent()) {
            // Transactions require control of the retry strategy
            throw new IllegalArgumentException("Cannot specify retryStrategy() if using asTransaction() on QueryOptions");
        }

        CoreSingleQueryTransactionOptions queryOpts = opts.asTransactionOptions();
        CoreTransactionsReactive tri = new CoreTransactionsReactive(core,
                CoreTransactionsConfig.createForSingleQueryTransactions(queryOpts == null ? DEFAULT_TRANSACTION_DURABILITY_LEVEL : queryOpts.durabilityLevel().orElse(DEFAULT_TRANSACTION_DURABILITY_LEVEL),
                        opts.timeout().orElse(DEFAULT_TRANSACTION_TIMEOUT),
                        queryOpts == null ? null : queryOpts.attemptContextFactory().orElse(null),
                        queryOpts == null ? Optional.empty() : queryOpts.metadataCollection()));
        final JsonObject json = JsonObject.create();
        opts.injectParams(json);
        try {
            ObjectNode converted = Mapper.reader().readValue(json.toBytes(), ObjectNode.class);
            JsonSerializer serializer = opts.serializer() == null ? environment.jsonSerializer() : opts.serializer();

            return tri.queryBlocking(statement, bucketName, scopeName, converted, opts.parentSpan())
                    .map(qr -> new QueryResult(qr.header, qr.rows, qr.trailer, serializer))
                    .onErrorResume(ErrorUtil::convertTransactionFailedInternal);
        } catch (IOException e) {
            return Mono.error(new EncodingFailureException(e));
        }
    }


    public static Mono<ReactiveQueryResult> singleQueryTransactionStreaming(Core core,
                                                                            ClusterEnvironment environment,
                                                                            String statement,
                                                                            @Nullable String bucketName,
                                                                            @Nullable String scopeName,
                                                                            QueryOptions.Built opts,
                                                                            Consumer<RuntimeException> errorConverter) {
        if (opts.retryStrategy().isPresent()) {
            // Transactions require control of the retry strategy
            throw new IllegalArgumentException("Cannot specify retryStrategy() if using asTransaction() on QueryOptions");
        }

        CoreSingleQueryTransactionOptions queryOpts = opts.asTransactionOptions();
        CoreTransactionsReactive tri = new CoreTransactionsReactive(core,
                CoreTransactionsConfig.createForSingleQueryTransactions(queryOpts == null ? DEFAULT_TRANSACTION_DURABILITY_LEVEL : queryOpts.durabilityLevel().orElse(DEFAULT_TRANSACTION_DURABILITY_LEVEL),
                        opts.timeout().orElse(DEFAULT_TRANSACTION_TIMEOUT),
                        queryOpts == null ? null : queryOpts.attemptContextFactory().orElse(null),
                        queryOpts == null ? Optional.empty() : queryOpts.metadataCollection()));
        final JsonObject json = JsonObject.create();
        opts.injectParams(json);
        try {
            ObjectNode converted = Mapper.reader().readValue(json.toBytes(), ObjectNode.class);
            JsonSerializer serializer = opts.serializer() == null ? environment.jsonSerializer() : opts.serializer();
            return tri.query(statement, bucketName, scopeName, converted, opts.parentSpan(), errorConverter)
                    .map(qr -> new ReactiveQueryResult(qr, serializer))
                    .onErrorResume(ErrorUtil::convertTransactionFailedInternal);
        } catch (IOException e) {
            return Mono.error(new EncodingFailureException(e));
        }
    }

}

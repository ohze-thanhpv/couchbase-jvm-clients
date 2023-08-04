/*
 * Copyright (c) 2019 Couchbase, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.couchbase.client.core.msg.kv;

import com.couchbase.client.core.CoreContext;
import com.couchbase.client.core.deps.io.netty.buffer.ByteBuf;
import com.couchbase.client.core.deps.io.netty.buffer.ByteBufAllocator;
import com.couchbase.client.core.io.CollectionIdentifier;
import com.couchbase.client.core.io.netty.kv.KeyValueChannelContext;
import com.couchbase.client.core.io.netty.kv.MemcacheProtocol;
import com.couchbase.client.core.msg.TargetedRequest;
import com.couchbase.client.core.msg.UnmonitoredRequest;
import com.couchbase.client.core.node.NodeIdentifier;
import com.couchbase.client.core.retry.RetryStrategy;

import java.time.Duration;
import java.util.Map;

import static com.couchbase.client.core.io.netty.kv.MemcacheProtocol.bodyAsBytes;
import static com.couchbase.client.core.io.netty.kv.MemcacheProtocol.decodeStatus;
import static com.couchbase.client.core.io.netty.kv.MemcacheProtocol.noBody;
import static com.couchbase.client.core.io.netty.kv.MemcacheProtocol.noCas;
import static com.couchbase.client.core.io.netty.kv.MemcacheProtocol.noDatatype;
import static com.couchbase.client.core.io.netty.kv.MemcacheProtocol.noExtras;
import static com.couchbase.client.core.io.netty.kv.MemcacheProtocol.noKey;
import static com.couchbase.client.core.io.netty.kv.MemcacheProtocol.noPartition;
import static com.couchbase.client.core.logging.RedactableArgument.redactMeta;
import static com.couchbase.client.core.logging.RedactableArgument.redactSystem;
import static java.util.Objects.requireNonNull;

public class CarrierBucketConfigRequest
  extends BaseKeyValueRequest<CarrierBucketConfigResponse>
  implements TargetedRequest, UnmonitoredRequest, ConfigRequest {

  private final NodeIdentifier target;
  private final Purpose purpose;

  public CarrierBucketConfigRequest(
    final Duration timeout,
    final CoreContext ctx,
    CollectionIdentifier collectionIdentifier,
    final RetryStrategy retryStrategy,
    final NodeIdentifier target,
    final Purpose purpose
  ) {
    super(timeout, ctx, retryStrategy, null, collectionIdentifier);
    this.target = target;
    this.purpose = requireNonNull(purpose);
  }

  @Override
  public ByteBuf encode(ByteBufAllocator alloc, int opaque, KeyValueChannelContext ctx) {
    return MemcacheProtocol.request(alloc, MemcacheProtocol.Opcode.GET_CONFIG, noDatatype(),
      noPartition(), opaque, noCas(), noExtras(), noKey(), noBody());
  }

  @Override
  public CarrierBucketConfigResponse decode(final ByteBuf response, KeyValueChannelContext ctx) {
    byte[] content = bodyAsBytes(response);
    return new CarrierBucketConfigResponse(decodeStatus(response), content);
  }

  @Override
  public NodeIdentifier target() {
    return target;
  }

  public Purpose purpose() {
    return purpose;
  }

  @Override
  public boolean idempotent() {
    return true;
  }

  @Override
  public Map<String, Object> serviceContext() {
    final Map<String, Object> ctx = super.serviceContext();
    if (target != null) {
      ctx.put("target", redactSystem(target.address()));
    }
    return ctx;
  }

  @Override
  public String name() {
    return "carrier_bucket_config";
  }

  @Override
  public String toString() {
    return "CarrierBucketConfigRequest{" +
      "target=" + redactSystem(target.address()) +
      ", bucket=" + redactMeta(collectionIdentifier().bucket()) +
      '}';
  }
}

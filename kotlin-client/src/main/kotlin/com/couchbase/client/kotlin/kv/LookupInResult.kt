/*
 * Copyright 2021 Couchbase, Inc.
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

package com.couchbase.client.kotlin.kv

import com.couchbase.client.core.api.kv.CoreSubdocGetResult
import com.couchbase.client.core.error.subdoc.PathMismatchException
import com.couchbase.client.core.msg.kv.SubDocumentField
import com.couchbase.client.kotlin.codec.JsonSerializer
import com.couchbase.client.kotlin.codec.TypeRef
import com.couchbase.client.kotlin.codec.typeRef
import com.couchbase.client.kotlin.internal.toStringUtf8

public class LookupInReplicaResult internal constructor(
    core: CoreSubdocGetResult,
    defaultSerializer: JsonSerializer,
    spec: LookupInSpec,
) : LookupInResult(core, defaultSerializer, spec) {
    /**
     * True means the result came from a replica; false means it came from the active.
     */
    public val replica: Boolean = core.replica()
}

public open class LookupInResult internal constructor(
    private val core: CoreSubdocGetResult,
    private val defaultSerializer: JsonSerializer,
    private val spec: LookupInSpec,
) {
    public val id: String = core.key()
    public val cas: Long = core.cas()
    public val deleted: Boolean = core.tombstone()

    @Deprecated("This property is useless. Please do not use it.", level = DeprecationLevel.ERROR)
    public val size: Int = spec.commands.size

    // When the LookupInResult is in scope as a receiver,
    // Subdoc instances have these addition properties / methods
    // for accessing field values.
    public val SubdocExists.value: Boolean get() = exists(spec, index)
    public val SubdocCount.value: Int get() = content(this).toStringUtf8().toInt()
    public val Subdoc.contentAsBytes: ByteArray get() = content(this)
    public val Subdoc.exists: Boolean get() = exists(spec, index)
    public inline fun <reified T> Subdoc.contentAs(serializer: JsonSerializer? = null): T = internalContentAs(this, typeRef(), serializer)

    @PublishedApi
    internal fun <T> internalContentAs(subdoc: Subdoc, type: TypeRef<T>, serializer: JsonSerializer? = null): T {
        return (serializer ?: defaultSerializer).deserialize(content(subdoc), type)
    }

    private fun exists(spec: LookupInSpec, index: Int): Boolean {
        checkSpec(spec)
        return try {
            core.exists(index)
        } catch (e: PathMismatchException) {
            // Kotlin SDK diverges from spec.
            // See https://issues.couchbase.com/browse/KCBC-119
            false;
        }
    }

    internal operator fun get(index: Int): SubDocumentField {
        return core.field(index).also { it.throwErrorIfPresent() }
    }

    internal fun content(subdoc: Subdoc): ByteArray {
        checkSpec(subdoc.spec)
        return get(subdoc.index).value()
    }

    internal fun content(subdoc: SubdocCount): ByteArray {
        checkSpec(subdoc.spec)
        return get(subdoc.index).value()
    }

    private fun checkSpec(spec: LookupInSpec) {
        require(spec == this.spec) { "Subdoc was not created from the same LookupInSpec as this result." }
    }
}

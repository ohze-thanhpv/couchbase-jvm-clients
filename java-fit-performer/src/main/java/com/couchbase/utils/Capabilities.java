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
package com.couchbase.utils;

import com.couchbase.client.protocol.sdk.Caps;

import java.util.ArrayList;
import java.util.List;

public class Capabilities {
    public static List<Caps> sdkImplementationCaps() {
        var out = new ArrayList<Caps>();

        // [start:3.1.5]
        out.add(Caps.SDK_PRESERVE_EXPIRY);
        // [end:3.1.5]

        // [start:3.4.1]
        out.add(Caps.SDK_KV_RANGE_SCAN);
        // [end:3.4.1]

        // We're not performance testing query index management, so not worth the complexity
        // of keeping them compiling.
        // [start:3.4.3]
        out.add(Caps.SDK_QUERY_INDEX_MANAGEMENT);
        out.add(Caps.SDK_COLLECTION_QUERY_INDEX_MANAGEMENT);
        // [end:3.4.3]

        // Actually the SDK has had various forms of FTS since 3.0.0, but we're not performance testing it currently
        // so it's not worth trying to keep the various flavours compiling.
        // [start:3.4.5]
        out.add(Caps.SDK_SEARCH);
        out.add(Caps.SDK_SEARCH_INDEX_MANAGEMENT);
        out.add(Caps.SDK_SCOPE_SEARCH);
        out.add(Caps.SDK_SCOPE_SEARCH_INDEX_MANAGEMENT);
        // [end:3.4.5]

        return out;
    }
}
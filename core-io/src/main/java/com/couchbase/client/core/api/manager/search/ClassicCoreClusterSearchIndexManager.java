/*
 * Copyright 2023 Couchbase, Inc.
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

package com.couchbase.client.core.api.manager.search;

import com.couchbase.client.core.Core;
import com.couchbase.client.core.annotation.Stability;

@Stability.Internal
public class ClassicCoreClusterSearchIndexManager extends ClassicCoreBaseSearchIndexManager {
  @Stability.Internal
  public ClassicCoreClusterSearchIndexManager(Core core) {
    super(core);
  }

  @Override
  String indexesPath() {
    return "/api/index";
  }
}

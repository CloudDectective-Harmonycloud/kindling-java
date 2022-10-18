/*
 * Copyright 2022 The Kindling Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.kindling.plugin.traceid.sw;

import org.apache.skywalking.apm.agent.core.context.TracingContext;

import io.kindling.agent.api.AdviceConfig;
import io.kindling.agent.api.AfterAdvice;
import io.kindling.agent.api.JoinPoint;
import io.kindling.agent.api.KindlingApi;
import io.kindling.agent.instrument.annotation.AdvicePointCut;

@AdvicePointCut(
    matchClasses = "org.apache.skywalking.apm.agent.core.context.TracingContext",
    matchMethods = "stopSpan",
    matchParams = "(org.apache.skywalking.apm.agent.core.context.trace.AbstractSpan)"
)
public class StopTraceAdvice implements AfterAdvice {
    private final AdviceConfig ADVICE_CONFIG;
    private SwAdapter adapter;

    public StopTraceAdvice() {
        ADVICE_CONFIG = new AdviceConfig().enableThisParam().enableReturnObjectParam();
    }

    public void after(JoinPoint joinPoint) {
        Boolean finish = (Boolean) joinPoint.getReturnObject();
        if (finish != null && finish) {
            if (adapter == null) {
                adapter = SwAdapter.getAdapter(TracingContext.class);
            }
            KindlingApi.exit(adapter.getReadableTraceId((TracingContext) joinPoint.getThis()));
        }
    }

    public AdviceConfig getAdviceConfig() {
        return ADVICE_CONFIG;
    }
}

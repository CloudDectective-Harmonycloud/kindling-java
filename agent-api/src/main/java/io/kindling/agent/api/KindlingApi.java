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

package io.kindling.agent.api;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class KindlingApi {
    private static final int TYPE_ENTRY_ENTER = 1;
    private static final int TYPE_ENTRY_EXIT = 0;

    private static final List<String> IGNORE_TRACES = Arrays.asList("Ignored_Trace");

    private static FileOutputStream out;

    static {
        String fileName = System.getProperty("kindling_out", "/dev/null");
        try {
            File devNull = new File(fileName);
            if (devNull.exists()) {
                out = new FileOutputStream(devNull);
            }
        } catch (IOException e) {
        }
        if (out == null) {
            System.err.println("[File Not Exist] " + fileName);
        }
    }

    public static void enter(String traceId) {
        if (out == null) {
            return;
        }
        recordTraceId(traceId, TYPE_ENTRY_ENTER);
    }

    public static void exit(String traceId) {
        if (out == null) {
            return;
        }
        recordTraceId(traceId, TYPE_ENTRY_EXIT);
    }

    private static void recordTraceId(String traceId, int type) {
        if (IGNORE_TRACES.contains(traceId)) {
            return;
        }
        synchronized (out) {
            try {
                out.write(String.format("kd-txid@%s!%d!", traceId, type).getBytes());
                out.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * v0 kd-span@durationNs!spanName!traceId!
     * => v1 kd-span@version!startTimeNs!spanName!traceId!
     */
    public static void recordSpan(String traceId, String spanName, long startTimeMs) {
        if (out == null) {
            return;
        }
        synchronized (out) {
            try {
                out.write(String.format("kd-span@%d!%d!%s!%s!", 1, startTimeMs * 1000000L, spanName, traceId).getBytes());
                out.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}

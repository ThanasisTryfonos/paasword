/*
 * Copyright 2016 PaaSword Framework, http://www.paasword.eu/
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
package eu.paasword.annotation.interpreter.c2j.json;

import com.google.common.collect.Maps;
import com.google.gson.internal.Primitives;
import java.util.Map;
import org.objectweb.asm.Type;

public class AnnotationInstanceJson {

    // annotations cannot have generic types
    private String type;
    private Map<String, Value> args;

    public void setType(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }

    public void addArg(String name, Value arg) {
        if (args == null)
            args = Maps.newHashMap();
        args.put(name, arg);
    }

    public static class Value {

        private String type;
        private Object value;

        public Value(String type, Object value) {
            if (value instanceof Type) {
                type = "java.lang.Class";
                value = ((Type) value).getClassName();
            } else if (Primitives.isWrapperType(value.getClass())) {
                type = Primitives.unwrap(value.getClass()).getCanonicalName();
            }
            this.type = type;
            this.value = value;
        }

        public String getType() {
            return type;
        }

        public Object getValue() {
            return value;
        }
    }

    public Map<String, Value> getArgs() {
        return args;
    }
}
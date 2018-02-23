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

import com.google.common.collect.Lists;
import java.util.Arrays;
import java.util.List;

public class TypeJson extends BaseJson {

    private String type;
    private List<TypeJson> args;

    public TypeJson() {
    }

    public TypeJson(String type, TypeJson... args) {
        this.type = type;
        if (args.length > 0) {
            this.args = Arrays.asList(args);
        }
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void addArg(TypeJson t) {
        if (args == null)
            args = Lists.newArrayList();
        args.add(t);
    }
}
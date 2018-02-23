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
import com.google.common.collect.Maps;
import com.google.gson.annotations.SerializedName;
import java.util.List;
import java.util.Map;

public class CallableJson extends NamedJson {

    private List<VariableJson> args;
    private Map<String, TypeJson> typeVariables;
    @SerializedName("throws")
    private List<TypeJson> exceptions;

    public void addArgument(VariableJson v) {
        if (args == null)
            args = Lists.newArrayList();
        if (v.getName() == null)
            v.setName("arg" + args.size());
        args.add(v);
    }

    public void addArgument(String t) {
        VariableJson v = new VariableJson();
        v.setType(new TypeJson(t));
        addArgument(v);
    }

    public VariableJson getArgument(int i) {
        return args.get(i);
    }

    public void addTypeVar(String name, TypeJson t) {
        if (typeVariables == null)
            typeVariables = Maps.newHashMap();
        typeVariables.put(name, t);
    }

    public void addException(TypeJson e) {
        if (exceptions == null)
            exceptions = Lists.newArrayList();
        exceptions.add(e);
    }

    public int getParameterCount() {
        if (args == null)
            return 0;
        return args.size();
    }

}
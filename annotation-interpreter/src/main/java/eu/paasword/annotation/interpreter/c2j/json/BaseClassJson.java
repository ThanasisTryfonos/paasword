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
import java.util.List;
import java.util.Map;

public abstract class BaseClassJson extends NamedJson {

    private List<MethodJson> methods;
    private String outerClass;
    private List<String> innerClasses;
    private Map<String, TypeJson> typeArgs;

    public void addMethod(MethodJson m) {
        if (methods == null)
            methods = Lists.newArrayList();
        methods.add(m);
    }

    public void addInnerClass(String c) {
        if (innerClasses == null)
            innerClasses = Lists.newArrayList();
        innerClasses.add(c);
    }

    public void addTypeArg(String name, TypeJson t) {
        if (typeArgs == null) {
            typeArgs = Maps.newHashMap();
        }
        typeArgs.put(name, t);
    }

    public void setOuterClass(String className) {
        outerClass = className;
    }

    public String getOuterClass() {
        return outerClass;
    }

    public List<MethodJson> getMethods() {
        return methods;
    }

    public Map<String, TypeJson> getTypeArgs() {
        return typeArgs;
    }

    public List<String> getInnerClasses() {
        return innerClasses;
    }
}
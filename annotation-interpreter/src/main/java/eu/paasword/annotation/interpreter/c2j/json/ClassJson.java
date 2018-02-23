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
import com.google.gson.annotations.SerializedName;
import java.util.List;

public class ClassJson extends BaseClassJson {

    @SerializedName("extends")
    private TypeJson superClass;
    @SerializedName("implements")
    private List<TypeJson> interfaces;
    private List<VariableJson> fields;
    private List<CallableJson> constructors;

    public void setSuperClass(String superClass) {
        setSuperClass(new TypeJson(superClass));
    }

    public void setSuperClass(TypeJson superClass) {
        this.superClass = superClass;
    }

    public void addInterface(String i) {
        addInterface(new TypeJson(i));
    }

    public void addInterface(TypeJson i) {
        if (interfaces == null)
            interfaces = Lists.newArrayList();
        interfaces.add(i);
    }

    public void addField(VariableJson f) {
        if (fields == null) {
            fields = Lists.newArrayList();
        }
        fields.add(f);
    }

    public void addConstructor(CallableJson c) {
        if (constructors == null) {
            constructors = Lists.newArrayList();
        }
        constructors.add(c);
    }

}
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
import java.util.List;

public class PackageJson extends BaseJson {

    private List<ClassJson> classes;
    private List<InterfaceJson> interfaces;
    private List<AnnotationJson> annotations;
    private List<EnumJson> enums;

    public void addClass(ClassJson c) {
        if (classes == null)
            classes = Lists.newArrayList();
        classes.add(c);
    }

    public void addInterface(InterfaceJson i) {
        if (interfaces == null)
            interfaces = Lists.newArrayList();
        interfaces.add(i);
    }

    public void addAnnotation(AnnotationJson a) {
        if (annotations == null)
            annotations = Lists.newArrayList();
        annotations.add(a);
    }

    public void addEnum(EnumJson e) {
        if (enums == null)
            enums = Lists.newArrayList();
        enums.add(e);
    }

    public List<ClassJson> getClasses() {
        return classes;
    }

    public List<EnumJson> getEnums() {
        return enums;
    }

    public List<AnnotationJson> getAnnotations() {
        return annotations;
    }

    public List<InterfaceJson> getInterfaces() {
        return interfaces;
    }

}
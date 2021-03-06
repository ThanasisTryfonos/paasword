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
package eu.paasword.annotation.interpreter.c2j.asm;

import com.google.common.collect.Lists;
import eu.paasword.annotation.interpreter.c2j.json.AnnotationInstanceJson;
import java.util.List;

public class AnnotationArrayVisitor extends BaseAnnotationVisitor {

    private List<AnnotationInstanceJson.Value> list = Lists.newArrayList();

    @Override
    protected void add(String name, String type, Object value) {
        list.add(new AnnotationInstanceJson.Value(type, value));
    }

    public List<AnnotationInstanceJson.Value> getList() {
        return list;
    }
}
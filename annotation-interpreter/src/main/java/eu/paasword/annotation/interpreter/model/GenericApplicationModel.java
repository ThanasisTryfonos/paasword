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
package eu.paasword.annotation.interpreter.model;

import eu.paasword.annotation.interpreter.c2j.json.ClassJson;

import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author smantzouratos
 */
public enum GenericApplicationModel {

    INSTANCE;

    private Map<String, Class> classModel = null;

    GenericApplicationModel() {
        this.classModel = new HashMap<>();
    }

    public Map<String, Class> getClassModel() {

        return this.classModel;
    }

    public void addClassToMap(String classVars, Class classModel) {

        // Check if already exists TODO

        if (!this.classModel.containsKey(classVars)) {
            this.classModel.put(classVars, classModel);
        }

    }

}

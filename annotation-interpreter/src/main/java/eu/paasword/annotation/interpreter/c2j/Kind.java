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
package eu.paasword.annotation.interpreter.c2j;

import eu.paasword.annotation.interpreter.c2j.json.AnnotationJson;
import eu.paasword.annotation.interpreter.c2j.json.BaseClassJson;
import eu.paasword.annotation.interpreter.c2j.json.ClassJson;
import eu.paasword.annotation.interpreter.c2j.json.EnumJson;
import eu.paasword.annotation.interpreter.c2j.json.InterfaceJson;
import org.objectweb.asm.Opcodes;

public enum Kind {
    
    ENUM(Opcodes.ACC_ENUM, EnumJson.class),
    ANNOTATION(Opcodes.ACC_ANNOTATION, AnnotationJson.class),
    INTERFACE(Opcodes.ACC_INTERFACE, InterfaceJson.class),
    CLASS(0, ClassJson.class),;

    private int flag;
    private Class<? extends BaseClassJson> jsonClass;

    private Kind(int flag, Class<? extends BaseClassJson> json) {
        this.flag = flag;
        this.jsonClass = json;
    }

    public static Kind getKind(int access) {
        for (Kind k : values()) {
            if ((access & k.flag) == k.flag)
                return k;
        }
        return CLASS;
    }

    public BaseClassJson newJson() {
        try {
            return jsonClass.newInstance();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
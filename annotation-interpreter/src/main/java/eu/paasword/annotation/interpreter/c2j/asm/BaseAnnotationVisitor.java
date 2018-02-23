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

import eu.paasword.annotation.interpreter.c2j.json.AnnotationInstanceJson;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.Type;

public abstract class BaseAnnotationVisitor extends AnnotationVisitor {

    public BaseAnnotationVisitor() {
        super(Opcodes.ASM5);
    }

    protected abstract void add(String name, String type, Object value);

    @Override
    public void visit(String name, Object value) {
        add(name, value.getClass().getName(), value);
    }

    @Override
    public void visitEnum(String name, String desc, String value) {
        add(name, Type.getType(desc).getClassName(), value);
    }

    @Override
    public AnnotationVisitor visitArray(String name) {
        AnnotationArrayVisitor av = new AnnotationArrayVisitor();
        add(name, "array", av.getList());
        return av;
    }

    @Override
    public AnnotationVisitor visitAnnotation(String name, String desc) {
        AnnotationInstanceJson anno = new AnnotationInstanceJson();
        add(name, Type.getType(desc).getClassName(), anno);
        return new AnnotationJsonVisitor(anno);
    }

}
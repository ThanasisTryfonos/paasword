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
import com.google.common.collect.Maps;
import java.util.List;
import java.util.Map;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.signature.SignatureVisitor;

public class ClassSignatureNode extends SignatureVisitor {

    public Map<String, TypeSignatureNode> typeArgs = Maps.newHashMap();
    public TypeSignatureNode superClass;
    public List<TypeSignatureNode> interfaces = Lists.newArrayList();

    private TypeSignatureNode current;

    public ClassSignatureNode() {
        super(Opcodes.ASM5);
    }

    @Override
    public void visitFormalTypeParameter(String name) {
        current = new TypeSignatureNode();
        typeArgs.put(name, current);
    }

    @Override
    public void visitClassType(String name) {
        current.type = name.replaceAll("[/$]", ".");
    }

    @Override
    public void visitInnerClassType(String name) {
        current.type = name.replaceAll("[/$]", ".");
    }

    @Override
    public SignatureVisitor visitTypeArgument(char wildcard) {
        return current;
    }

    @Override
    public SignatureVisitor visitSuperclass() {
        return superClass = new TypeSignatureNode();
    }

    @Override
    public SignatureVisitor visitInterfaceBound() {
        TypeSignatureNode i = new TypeSignatureNode();
        interfaces.add(i);
        return i;
    }
}
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

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import eu.paasword.annotation.interpreter.c2j.json.TypeJson;
import java.util.List;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.signature.SignatureVisitor;


public class TypeSignatureNode extends SignatureVisitor {

    public String type;
    public List<TypeSignatureNode> typeArgs = Lists.newArrayList();

    public TypeSignatureNode() {
        super(Opcodes.ASM5);
    }

    @Override
    public void visitTypeVariable(String name) {
        this.type = name;
    }

    @Override
    public void visitBaseType(char descriptor) {
        this.type = Type.getType(descriptor + "").getClassName();
    }

    @Override
    public SignatureVisitor visitArrayType() {
        TypeSignatureNode t = new TypeSignatureNode();
        this.typeArgs.add(t);
        return t;
    }

    @Override
    public void visitClassType(String name) {
        type = name.replaceAll("[/$]", ".");
    }

    @Override
    public void visitInnerClassType(String name) {
        type = name.replaceAll("[/$]", ".");
    }

    @Override
    public SignatureVisitor visitTypeArgument(char wildcard) {
        TypeSignatureNode t = new TypeSignatureNode();
        this.typeArgs.add(t);
        return t;
    }

    @Override
    public String toString() {
        String type = Type.getType(this.type).getClassName();
        if (type == null)
            type = this.type;
        StringBuilder sb = new StringBuilder(type);
        if (!typeArgs.isEmpty()) {
            sb.append("<");
            Joiner.on(", ").appendTo(sb, typeArgs);
            sb.append(">");
        }
        return sb.toString();
    }

    public void accept(TypeJson json) {
        json.setType(type);
        for (TypeSignatureNode n : typeArgs) {
            TypeJson arg = new TypeJson();
            json.addArg(arg);
            n.accept(arg);
        }
    }
}
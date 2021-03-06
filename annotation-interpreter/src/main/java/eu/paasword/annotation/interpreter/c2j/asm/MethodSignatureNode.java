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
import com.google.common.collect.Maps;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.signature.SignatureVisitor;

public class MethodSignatureNode extends SignatureVisitor {

    public TypeSignatureNode returnType;
    public List<TypeSignatureNode> parameters = Lists.newArrayList();
    public List<TypeSignatureNode> exceptions = Lists.newArrayList();
    public Map<String, TypeSignatureNode> typeVars = Maps.newHashMap();

    private TypeSignatureNode currentVar;

    public MethodSignatureNode() {
        super(Opcodes.ASM5);
    }

    @Override
    public void visitFormalTypeParameter(String name) {
        currentVar = new TypeSignatureNode();
        currentVar.type = name;
        typeVars.put(name, currentVar);
    }

    @Override
    public void visitClassType(String name) {
        currentVar.type = name;
    }

    @Override
    public void visitInnerClassType(String name) {
        currentVar.type = name;
    }

    @Override
    public SignatureVisitor visitTypeArgument(char wildcard) {
        TypeSignatureNode t = new TypeSignatureNode();
        currentVar.typeArgs.add(t);
        return t;
    }

    @Override
    public SignatureVisitor visitParameterType() {
        TypeSignatureNode p = new TypeSignatureNode();
        parameters.add(p);
        return p;
    }

    @Override
    public SignatureVisitor visitReturnType() {
        return returnType = new TypeSignatureNode();
    }

    @Override
    public SignatureVisitor visitExceptionType() {
        TypeSignatureNode e = new TypeSignatureNode();
        exceptions.add(e);
        return e;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (typeVars.size() > 0) {
            sb.append("<");
            boolean b = false;
            for (Entry<String, TypeSignatureNode> ent : typeVars.entrySet()) {
                if (b)
                    sb.append(", ");
                sb.append(ent.getKey());
                if (ent.getValue().type != null) {

                    sb.append(" extends ").append(ent.getValue());
                }
                b = true;
            }
            sb.append("> ");
        }
        sb.append("(");
        Joiner.on(", ").appendTo(sb, parameters);
        sb.append(")").append(returnType);
        return sb.toString();
    }
}
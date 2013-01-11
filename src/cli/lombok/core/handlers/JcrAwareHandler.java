/*
 * Copyright 2012-2013 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package lombok.core.handlers;

import lombok.ast.Expression;
import lombok.ast.IMethod;
import lombok.ast.IType;

import static lombok.ast.AST.*;

/**
 * @author Andres Almiray
 */
public abstract class JcrAwareHandler<TYPE_TYPE extends IType<? extends IMethod<?, ?, ?, ?>, ?, ?, ?, ?, ?>> extends AbstractHandler<TYPE_TYPE> implements JcrAwareConstants {
    private Expression<?> defaultJcrProviderInstance() {
        return Call(Name(DEFAULT_JCR_PROVIDER_TYPE), "getInstance");
    }

    public void addJcrProviderField(final TYPE_TYPE type) {
        addField(type, JCR_PROVIDER_TYPE, JCR_PROVIDER_FIELD_NAME, defaultJcrProviderInstance());
    }

    public void addJcrProviderAccessors(final TYPE_TYPE type) {
        type.editor().injectMethod(
            MethodDecl(Type(VOID), METHOD_SET_JCR_PROVIDER)
                .makePublic()
                .withArgument(Arg(Type(JCR_PROVIDER_TYPE), PROVIDER))
                .withStatement(
                    If(Equal(Name(PROVIDER), Null()))
                        .Then(Block()
                            .withStatement(Assign(Field(JCR_PROVIDER_FIELD_NAME), defaultJcrProviderInstance())))
                        .Else(Block()
                            .withStatement(Assign(Field(JCR_PROVIDER_FIELD_NAME), Name(PROVIDER)))))
        );

        type.editor().injectMethod(
            MethodDecl(Type(JCR_PROVIDER_TYPE), METHOD_GET_JCR_PROVIDER)
                .makePublic()
                .withStatement(Return(Field(JCR_PROVIDER_FIELD_NAME)))
        );
    }

    public void addJcrContributionMethods(final TYPE_TYPE type) {
        delegateMethodsTo(type, METHODS, Field(JCR_PROVIDER_FIELD_NAME));
    }
}

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

package lombok.eclipse.handlers;

import griffon.plugins.jcr.JcrAware;
import lombok.core.AnnotationValues;
import lombok.core.handlers.JcrAwareConstants;
import lombok.core.handlers.JcrAwareHandler;
import lombok.eclipse.EclipseAnnotationHandler;
import lombok.eclipse.EclipseNode;
import lombok.eclipse.handlers.ast.EclipseType;
import org.eclipse.jdt.internal.compiler.ast.Annotation;

import static lombok.core.util.ErrorMessages.canBeUsedOnClassAndEnumOnly;

/**
 * @author Andres Almiray
 */
public class HandleJcrAware extends EclipseAnnotationHandler<JcrAware> {
    private final EclipseJcrAwareHandler handler = new EclipseJcrAwareHandler();

    @Override
    public void handle(AnnotationValues<JcrAware> annotation, Annotation source, EclipseNode annotationNode) {
        EclipseType type = EclipseType.typeOf(annotationNode, source);
        if (type.isAnnotation() || type.isInterface()) {
            annotationNode.addError(canBeUsedOnClassAndEnumOnly(JcrAware.class));
            return;
        }

        EclipseUtil.addInterface(type.get(), JcrAwareConstants.JCR_CONTRIBUTION_HANDLER_TYPE, source);
        handler.addJcrProviderField(type);
        handler.addJcrProviderAccessors(type);
        handler.addJcrContributionMethods(type);
        type.editor().rebuild();
    }

    private static class EclipseJcrAwareHandler extends JcrAwareHandler<EclipseType> {
    }
}

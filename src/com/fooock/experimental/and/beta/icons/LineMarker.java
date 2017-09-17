/*
 * Copyright (c) 2017 newhouse <nhitbh at gmail dot com>
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

package com.fooock.experimental.and.beta.icons;

import com.intellij.codeHighlighting.Pass;
import com.intellij.codeInsight.daemon.LineMarkerInfo;
import com.intellij.codeInsight.daemon.LineMarkerProvider;
import com.intellij.openapi.editor.markup.GutterIconRenderer;
import com.intellij.openapi.util.IconLoader;
import com.intellij.psi.*;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.Collection;
import java.util.List;

/**
 * Plugin main entry point
 */
public final class LineMarker implements LineMarkerProvider {

    // Documentation:
    // --------------
    // https://www.jetbrains.org/intellij/sdk/docs/tutorials/custom_language_support/line_marker_provider.html

    private static final String EXPERIMENTAL_ANNOTATION_NAME = "Experimental";
    private static final String BETA_ANNOTATION_NAME = "Beta";

    private static final Icon ICON_EXPERIMENTAL_ANNOTATION = IconLoader.getIcon("/icons/explosion.png");
    private static final Icon ICON_BETA_ANNOTATION = IconLoader.getIcon("/icons/fire.png");

    private static final PsiAnnotation[] EMPTY_ANNOTATION_ARRAY = new PsiAnnotation[0];

    @Nullable
    @Override
    public LineMarkerInfo getLineMarkerInfo(@NotNull PsiElement psiElement) {
        // check if the element is a field
        if (isMethodParameters(psiElement)) {
            final PsiParameter[] methodParams = getParametersFrom(psiElement);
            for (PsiParameter parameter : methodParams) {
                // detect annotation by parameter
            }
        }

        // check if the element is a reference with method. Note that if the method reference
        // find an annotation, this method don't continue, if not, check the method params
        if (isReferenceWithMethod(psiElement)) {
            final PsiMethod method = getMethodFrom(psiElement);
            final PsiAnnotation[] annotationsFromMethod = getAnnotationsFrom(method);

            // if no annotations found return quickly!
            if (!hasAnnotations(annotationsFromMethod)) {
                return null;
            }
            return getLineMarkerInfo(psiElement, annotationsFromMethod);
        }
        return null;
    }

    /**
     * This method get the line marker for the current element when is know that it has annotations. If the
     * annotations found are know, this is, {@link #EXPERIMENTAL_ANNOTATION_NAME} or {@link #BETA_ANNOTATION_NAME},
     * the icon for each one is shown. If the annotations not match any of these two, this method return null
     *
     * @param psiElement            Current element
     * @param annotationsFromMethod Array of annotations from the element. Always more than zero
     * @return LineMarkerInfo if find know annotation or null if not found
     */
    @Nullable
    private LineMarkerInfo getLineMarkerInfo(@NotNull PsiElement psiElement,
                                             @NotNull PsiAnnotation[] annotationsFromMethod) {
        // at this point we can check if the existing annotations match
        for (PsiAnnotation annotation : annotationsFromMethod) {
            final String qualifiedName = annotation.getQualifiedName();
            if (qualifiedName == null || qualifiedName.isEmpty()) continue;
            final int index = qualifiedName.lastIndexOf(".");
            // if index is equal to -1, then check directly in the qualified name contain
            // Experimental or Beta
            if (index == -1) {
                if (!isAnnotationKnow(qualifiedName)) continue;
                return markerFromAnnotationName(qualifiedName, psiElement);
            }
            // Remove from the final annotation name the '.' (index + 1)
            final String annotationName = qualifiedName.substring(index + 1, qualifiedName.length()).trim();
            if (!isAnnotationKnow(annotationName)) continue;
            return markerFromAnnotationName(annotationName, psiElement);
        }
        return null;
    }

    /**
     * @param annotationName Current annotation
     * @return True if the annotation is know, false if not.
     * @see #BETA_ANNOTATION_NAME
     * @see #EXPERIMENTAL_ANNOTATION_NAME
     */
    private boolean isAnnotationKnow(String annotationName) {
        return annotationName.equals(EXPERIMENTAL_ANNOTATION_NAME) || annotationName.equals(BETA_ANNOTATION_NAME);
    }

    /**
     * Get the {@link LineMarkerInfo} for the given annotation type if is any of {@link #EXPERIMENTAL_ANNOTATION_NAME}
     * or {@link #BETA_ANNOTATION_NAME}, otherwise this method return null
     *
     * @param annotationName Annotation name
     * @param element        Current element
     * @return LineMarkerInfo of null
     */
    @Nullable
    private LineMarkerInfo markerFromAnnotationName(@NotNull String annotationName, @NotNull PsiElement element) {
        if (EXPERIMENTAL_ANNOTATION_NAME.equals(annotationName)) {
            // here return the line marker with the specified experimental icon
            return createLineMarkerFor(element, ICON_EXPERIMENTAL_ANNOTATION, EXPERIMENTAL_ANNOTATION_NAME);
        }
        if (BETA_ANNOTATION_NAME.equals(annotationName)) {
            // here return the line marker with the specified beta icon
            return createLineMarkerFor(element, ICON_BETA_ANNOTATION, BETA_ANNOTATION_NAME);
        }
        return null;
    }

    /**
     * Create the {@link LineMarkerInfo} for the given {@link PsiElement} with the required {@link Icon}. This
     * method never return null. Note that the line marker is aligned always to the left. When the mouse is
     * over the icon a tooltip indicating the name of the function and if is experimental or beta is shown
     *
     * @param element Current element
     * @param icon    Icon for the required annotation
     * @param type    Type of the line marker
     * @return LineMarkerInfo
     */
    @NotNull
    private LineMarkerInfo<PsiElement> createLineMarkerFor(@NotNull PsiElement element,
                                                           @NotNull Icon icon,
                                                           @NotNull @NonNls String type) {
        return new LineMarkerInfo<>(element, element.getTextRange(), icon, Pass.LINE_MARKERS,
                psiElement -> String.format("%s function %s detected", type, psiElement.getText()),
                null, GutterIconRenderer.Alignment.LEFT);
    }

    /**
     * Check if in the given array has more than zero elements
     *
     * @param annotations Array if annotations
     * @return True if more than zero elements, false otherwise
     */
    private boolean hasAnnotations(@NotNull PsiAnnotation[] annotations) {
        return annotations.length > 0;
    }

    /**
     * Get the current {@link PsiAnnotation}s from the given elements. Note that this method can return
     * empty elements
     *
     * @param psiElement Current element
     * @return Array of annotations from element if exists, empty if not
     */
    private PsiAnnotation[] getAnnotationsFrom(PsiElement psiElement) {
        if (psiElement instanceof PsiModifierListOwner) {
            final PsiModifierListOwner modifierListOwner = (PsiModifierListOwner) psiElement;
            final PsiModifierList modifiers = modifierListOwner.getModifierList();
            // modifiers can be null
            if (modifiers == null) {
                return EMPTY_ANNOTATION_ARRAY;
            }
            return modifiers.getAnnotations();
        }
        return EMPTY_ANNOTATION_ARRAY;
    }

    /**
     * Check if the given {@link PsiElement} is a method and has more than zero parameters
     *
     * @param psiElement Current element
     * @return True if the element is a field, false if not
     */
    private boolean isMethodParameters(PsiElement psiElement) {
        return psiElement instanceof PsiMethod
                && ((PsiMethod) psiElement).getParameterList().getParametersCount() > 0;
    }

    /**
     * Get the method parameters from the given {@link PsiElement}
     *
     * @param psiElement Current element
     * @return The method parameters
     */
    private PsiParameter[] getParametersFrom(@NotNull PsiElement psiElement) {
        return ((PsiMethod) psiElement).getParameterList().getParameters();
    }

    /**
     * Check if the given {@link PsiElement} is a valid reference and has a method. Note that this method
     * only return true when the element is a reference and has a method, for example:
     * <pre>
     *  MyObject obj = new MyObject();
     *  obj.test();
     * </pre>
     * This method return true when the {@code obj.pre()} is detected
     *
     * @param psiElement Current element
     * @return True if the element is a reference that has a method, false if not
     */
    private boolean isReferenceWithMethod(PsiElement psiElement) {
        return psiElement instanceof PsiReferenceExpression
                && ((PsiReferenceExpression) psiElement).resolve() instanceof PsiMethod;
    }

    /**
     * Get the {@link PsiMethod} from the given {@link PsiElement}. When this method is called is
     * because the {@link #isReferenceWithMethod(PsiElement)} return {@code true}
     *
     * @param psiElement Current element
     * @return The method of the current reference
     */
    private PsiMethod getMethodFrom(@NotNull PsiElement psiElement) {
        return (PsiMethod) ((PsiReferenceExpression) psiElement).resolve();
    }

    @Override
    public void collectSlowLineMarkers(@NotNull List<PsiElement> list,
                                       @NotNull Collection<LineMarkerInfo> collection) {
        // not used for the moment
    }
}

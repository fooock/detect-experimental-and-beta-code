package com.fooock.experimental.and.beta.icons;

import com.intellij.codeInsight.daemon.LineMarkerInfo;
import com.intellij.codeInsight.daemon.LineMarkerProvider;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiParameter;
import com.intellij.psi.PsiReferenceExpression;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;

/**
 * Plugin main entry point
 */
public final class LineMarker implements LineMarkerProvider {

    // Documentation:
    // --------------
    // https://www.jetbrains.org/intellij/sdk/docs/tutorials/custom_language_support/line_marker_provider.html

    @Nullable
    @Override
    public LineMarkerInfo getLineMarkerInfo(@NotNull PsiElement psiElement) {
        // check if the element is a field
        if (isMethodParameters(psiElement)) {
            final PsiParameter[] methodParams = getParametersFrom(psiElement);
            for (PsiParameter parameter : methodParams) {
                // detect annotations by parameter
            }
        }
        // check if the element is a reference with method
        if (isReferenceWithMethod(psiElement)) {
            final PsiMethod method = getMethodFrom(psiElement);
            // detect annotation by method
            final PsiParameter[] parameters = getParametersFrom(method);
            for (PsiParameter parameter : parameters) {
                // detect annotation by parameter
            }
        }
        return null;
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
    private PsiParameter[] getParametersFrom(PsiElement psiElement) {
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
    private PsiMethod getMethodFrom(PsiElement psiElement) {
        return (PsiMethod) ((PsiReferenceExpression) psiElement).resolve();
    }

    @Override
    public void collectSlowLineMarkers(@NotNull List<PsiElement> list,
                                       @NotNull Collection<LineMarkerInfo> collection) {
        // not used for the moment
    }
}

package org.jetbrains.kotlin.ui.editors.quickassist

import com.intellij.psi.PsiElement
import com.intellij.psi.util.PsiTreeUtil
import org.jetbrains.kotlin.psi.*

fun canFunctionOrGetterReturnExpression(functionOrGetter: KtDeclaration, expression: KtExpression): Boolean {
    return if (functionOrGetter is KtFunctionLiteral) {
        val functionLiteralBody = functionOrGetter.bodyExpression
        var returnedElement: PsiElement? = null
        if (functionLiteralBody != null) {
            val children = functionLiteralBody.children
            val length = children.size
            if (length > 0) {
                returnedElement = children[length - 1]
            }
        }
        returnedElement is KtExpression && canEvaluateTo(returnedElement as KtExpression?, expression)
    } else if (functionOrGetter is KtDeclarationWithInitializer && canEvaluateTo(
            functionOrGetter.initializer, expression
        )
    ) {
        true
    } else {
        val returnExpression = PsiTreeUtil.getParentOfType(
            expression, KtReturnExpression::class.java
        )
        returnExpression != null && canEvaluateTo(returnExpression.returnedExpression, expression)
    }
}

private fun canEvaluateTo(parent: KtExpression?, child: KtExpression?): Boolean {
    var child = child
    if (parent != null && child != null) {
        while (true) {
            while (parent !== child) {
                val childParent = child!!.parent
                if (childParent is KtParenthesizedExpression) {
                    child = childParent
                } else if (childParent is KtDotQualifiedExpression && (child is KtCallExpression || child is KtDotQualifiedExpression)) {
                    child = childParent
                } else {
                    child = getParentForBranch(child)
                    if (child == null) {
                        return false
                    }
                }
            }
            return true
        }
    } else {
        return false
    }
}

private fun getParentForBranch(expression: KtExpression?): KtExpression? {
    val parent: KtExpression? = getParentIfForBranch(expression)
    return (parent ?: getParentWhenForBranch(expression)) as KtExpression
}

private fun getParentIfForBranch(expression: KtExpression?): KtIfExpression? {
    val ifExpression = PsiTreeUtil.getParentOfType(
        expression, KtIfExpression::class.java, true
    )
    return if (ifExpression == null) {
        null
    } else {
        if (!equalOrLastInBlock(ifExpression.then, expression) && !equalOrLastInBlock(
                ifExpression.getElse(), expression
            )
        ) null else ifExpression
    }
}

private fun getParentWhenForBranch(expression: KtExpression?): KtWhenExpression? {
    val whenEntry = PsiTreeUtil.getParentOfType(
        expression, KtWhenEntry::class.java, true
    )
    return if (whenEntry == null) {
        null
    } else {
        val whenEntryExpression = whenEntry.expression
        if (whenEntryExpression == null) {
            null
        } else {
            if (!equalOrLastInBlock(
                    whenEntryExpression, expression
                )
            ) null else PsiTreeUtil.getParentOfType<KtWhenExpression>(
                whenEntry, KtWhenExpression::class.java, true
            )
        }
    }
}

private fun equalOrLastInBlock(block: KtExpression?, expression: KtExpression?): Boolean {
    return if (block === expression) {
        true
    } else {
        block is KtBlockExpression && expression?.parent === block && PsiTreeUtil.getNextSiblingOfType(
            expression, KtExpression::class.java
        ) == null
    }
}

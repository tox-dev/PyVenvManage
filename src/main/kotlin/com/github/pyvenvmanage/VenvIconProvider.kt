package com.github.pyvenvmanage

import javax.swing.Icon

import com.intellij.ide.IconLayerProvider
import com.intellij.ide.IconProvider
import com.intellij.openapi.util.Iconable
import com.intellij.psi.PsiDirectory
import com.intellij.psi.PsiElement

import com.jetbrains.python.icons.PythonIcons.Python.Virtualenv
import com.jetbrains.python.sdk.PythonSdkUtil

class VenvIconProvider :
    IconProvider(),
    IconLayerProvider {
    override fun getLayerDescription(): String = "Python virtual environment"

    override fun getIcon(
        element: PsiElement,
        flags: Int,
    ): Icon? = determineIcon(element)

    override fun getLayerIcon(
        element: Iconable,
        isLocked: Boolean,
    ): Icon? = determineIcon(element)

    private fun determineIcon(element: Any): Icon? {
        if (element is PsiDirectory) {
            val venvRootPath = element.virtualFile.path
            if (PythonSdkUtil.getPythonExecutable(venvRootPath) != null) {
                return Virtualenv
            }
        }
        return null
    }
}

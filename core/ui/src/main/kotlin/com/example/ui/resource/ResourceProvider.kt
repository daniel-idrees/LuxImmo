package com.example.ui.resource

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

interface ResourceProvider {
    fun getString(resId: Int, vararg args: Any): String
    fun getQuantityString(resId: Int, quantity: Int, vararg args: Any): String
}

@Singleton
internal class AndroidResourceProvider @Inject constructor(
    @ApplicationContext private val context: Context
): ResourceProvider {
    override fun getString(resId: Int, vararg args: Any): String {
        return context.getString(resId, *args)
    }

    override fun getQuantityString(resId: Int, quantity: Int, vararg args: Any): String {
        return context.resources.getQuantityString(resId, quantity, *args)
    }
}
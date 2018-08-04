package com.smackwerks.kotlinchannels.data

import com.github.kittinunf.fuel.core.ResponseDeserializable
import com.squareup.moshi.KotlinJsonAdapterFactory
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types

inline fun <reified T : Any> moshiArrayDeserializerOf() =  object : ResponseDeserializable<List<T>> {
    override fun deserialize(content: String): List<T>? =
        Moshi.Builder()
            .add(KotlinJsonAdapterFactory())
            .build()
            .adapter<List<T>>(Types.newParameterizedType(List::class.java, T::class.java))
            .fromJson(content)
}
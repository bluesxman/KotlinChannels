package com.smackwerks.kotlinchannels.data

import awaitObjectResult
import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.fuel.core.ResponseDeserializable
import com.github.kittinunf.fuel.moshi.moshiDeserializerOf
import com.github.kittinunf.result.Result
import com.squareup.moshi.KotlinJsonAdapterFactory
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import kotlinx.coroutines.experimental.channels.ArrayChannel
import kotlinx.coroutines.experimental.channels.Channel
import kotlinx.coroutines.experimental.launch
import timber.log.Timber
import com.squareup.moshi.Types.newParameterizedType



class RepoModel {
    fun getRepos(namePattern: Regex = Regex(".*"), lookAhead: Int = LOOKAHEAD): Channel<Repository> {
        val repos = ArrayChannel<Repository>(lookAhead)

        launch {
            var result = Fuel.get(URL).awaitObjectResult(deserializer)

            while (!repos.isClosedForSend) {
                if (result is Result.Success) {
                    for (r in result.value) {
                        if (namePattern.matches(r.name)) {
                            Timber.d("sending")
                            repos.send(r)
                        } else {
                            Timber.d("no match")
                        }
                    }
                    val lastRepoId = result.value.last().id
                    result = Fuel.get("$URL?since=$lastRepoId").awaitObjectResult(deserializer)
                } else {
                    Timber.e("Request failed, closing channel: $result")
                    repos.close()
                }
            }
        }

        return repos
    }

    companion object {
        private const val URL = "https://api.github.com/repositories"
        private const val LOOKAHEAD = 50
        private val deserializer =  object : ResponseDeserializable<List<Repository>> {
            override fun deserialize(content: String): List<Repository>? =
                Moshi.Builder()
                    .add(KotlinJsonAdapterFactory())
                    .build()
                    .adapter<List<Repository>>(Types.newParameterizedType(List::class.java, Repository::class.java))
                    .fromJson(content)
        }
    }
}

data class Repository(
    val id: Int,
    val name: String
)
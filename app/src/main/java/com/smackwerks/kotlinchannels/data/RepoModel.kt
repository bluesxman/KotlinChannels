package com.smackwerks.kotlinchannels.data

import awaitObjectResult
import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.result.Result
import kotlinx.coroutines.experimental.channels.ArrayChannel
import kotlinx.coroutines.experimental.channels.Channel
import kotlinx.coroutines.experimental.launch
import timber.log.Timber

object RepoModel {
    private const val URL = "https://api.github.com/repositories"
    private const val LOOKAHEAD = 50

    fun getRepos(namePattern: Regex? = null, lookAhead: Int = LOOKAHEAD): Channel<Repository> {
        val repos = ArrayChannel<Repository>(lookAhead)

        launch {
            val deserializer = moshiArrayDeserializerOf<Repository>()
            var result = Fuel.get(URL).awaitObjectResult(deserializer)

            while (!repos.isClosedForSend) {
                when (result) {
                    is Result.Success -> {
                        result.value
                            .filter { namePattern?.matches(it.name) ?: true }
                            .forEach { repos.send(it) }
                        val lastRepoId = result.value.last().id
                        result = Fuel.get("$URL?since=$lastRepoId").awaitObjectResult(deserializer)
                    }
                    is Result.Failure -> {
                        Timber.e("Request failed, closing channel: ${result.error}")
                        repos.close()
                    }
                }
            }
        }

        return repos
    }
}

data class Repository(
    val id: Int,
    val name: String
)
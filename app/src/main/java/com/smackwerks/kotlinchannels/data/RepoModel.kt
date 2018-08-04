package com.smackwerks.kotlinchannels.data

import awaitObjectResult
import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.result.Result
import kotlinx.coroutines.experimental.channels.ArrayChannel
import kotlinx.coroutines.experimental.channels.Channel
import kotlinx.coroutines.experimental.launch
import timber.log.Timber

class RepoModel {
    fun getRepos(namePattern: Regex? = null, lookAhead: Int = LOOKAHEAD): Channel<Repository> {
        val repos = ArrayChannel<Repository>(lookAhead)

        launch {
            val deserializer = moshiArrayDeserializerOf<Repository>()
            var result = Fuel.get(URL).awaitObjectResult(deserializer)

            while (!repos.isClosedForSend) {
                if (result is Result.Success) {
                    for (r in result.value) {
                        namePattern?.apply {
                            if (matches(r.name)) {
                                repos.send(r)
                            }
                        } ?: repos.send(r)
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
    }
}

data class Repository(
    val id: Int,
    val name: String
)
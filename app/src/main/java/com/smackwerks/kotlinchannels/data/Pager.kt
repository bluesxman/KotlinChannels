package com.smackwerks.kotlinchannels.data

import awaitObjectResponse
import com.github.kittinunf.fuel.core.Request
import com.github.kittinunf.fuel.core.ResponseDeserializable
import com.github.kittinunf.result.Result
import kotlinx.coroutines.experimental.channels.ArrayChannel
import kotlinx.coroutines.experimental.channels.Channel
import kotlinx.coroutines.experimental.launch

class Pager<T : Any, U : Any>(
    private val begin: () -> Request,
    private val next: (T) -> Request,
    private val process: ResponseDeserializable<T>,
    private val elements: (T) -> Iterable<U>,
    private val lookAhead: Int
) {
    fun start(): Channel<U> {
        val chan = ArrayChannel<U>(lookAhead)

        launch {
            var result = begin().awaitObjectResponse(process).third

            while (!chan.isClosedForSend) {
                if (result is Result.Success) {
                    for (e in elements(result.value)) {
                        chan.send(e)
                    }
                    result = next(result.value).awaitObjectResponse(process).third
                } else {
                    chan.close()
                }
            }
        }

        return chan
    }
}

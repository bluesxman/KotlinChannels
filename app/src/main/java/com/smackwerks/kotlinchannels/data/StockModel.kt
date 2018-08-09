package com.smackwerks.kotlinchannels.data

import awaitObjectResult
import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.fuel.moshi.moshiDeserializerOf
import com.github.kittinunf.result.Result
import com.smackwerks.kotlinchannels.data.response.IexStockPriceResponse
import kotlinx.coroutines.experimental.channels.Channel
import kotlinx.coroutines.experimental.channels.ConflatedChannel
import kotlinx.coroutines.experimental.delay
import kotlinx.coroutines.experimental.launch
import timber.log.Timber

object StockModel {
    private const val INTERVAL = 2000

    fun getStockPrice(symbol: String): Channel<Stock> {
        val chan = ConflatedChannel<Stock>()

        launch {
            while (!chan.isClosedForSend) {
                getLatest(symbol)?.let { chan.send(it) }
                delay(INTERVAL)
            }
        }

        return chan
    }

    suspend fun getLatest(symbol: String): Stock? {
        val url = "https://api.iextrading.com/1.0/stock/${symbol}/quote"
        val result = Fuel.get(url).awaitObjectResult(moshiDeserializerOf<IexStockPriceResponse>())
        return when (result) {
            is Result.Success -> with(result.value) {
                Timber.d("Got price")
                Stock(companyName, symbol, iexRealtimePrice)
            }
            is Result.Failure -> run {
                Timber.e(result.toString())
                null
            }
        }
    }
}

data class Stock(
    val company: String,
    val symbol: String,
    val price: Double
)
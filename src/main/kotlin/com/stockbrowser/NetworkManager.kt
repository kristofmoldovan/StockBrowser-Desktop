package com.stockbrowser

import com.stockbrowser.model.BarContainer
import com.stockbrowser.model.StockContainer
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.coroutines.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.serialization.jackson.*


object NetworkManager {

    private val client = HttpClient(CIO) {
        install(ContentNegotiation) {
            jackson()
        }
    }

    //API kulcsok
    lateinit var keys: Keys


    //Ellenőrzöm, hogy helyesek-e a kulcsok, majd a request választól függően a paraméterben megadott függvényeket hívom vissza.
    @OptIn(DelicateCoroutinesApi::class)
    fun checkApiKeys(keys: Keys, onSuccess: () -> Unit, onError:() -> Unit) {

        GlobalScope.launch(Dispatchers.Main) {
            var innerException: Throwable? = null
            try {
                val response = client.get("https://data.alpaca.markets/v2/stocks/trades/latest") {
                    headers {
                        append("Apca-Api-Key-Id", keys.apiKey!!)
                        append("Apca-Api-Secret-Key", keys.apiSecret!!)
                    }
                    url {
                        parameters.append("symbols", "TSLA")
                        parameters.append("feed", "iex")
                    }
                }


                if (response.status == HttpStatusCode.OK) {
                    println("API keys: ok")
                    try {
                        onSuccess()
                    } catch (e: Throwable) {
                        innerException = e
                    }
                } else {
                    println(response.bodyAsText())
                    throw Exception("API error")
                }
            } catch (e: Exception) {
                println(e.message)
                println(e.stackTrace)
                onError()
            } finally {
                if (innerException != null)
                    throw innerException
            }
        }

    }

    //Ellenörzöm, hogy létezik-e a részvény, majd a paraméterben megadott függvényeket hívom vissza, a hívás kimenetétől függően
    fun checkIfSymbolExists(symbol: String, onSuccess: () -> Unit, onFail: () -> Unit, onError: () -> Unit) {
        getLatestTrades(mutableListOf(symbol),{
            if (it.trades.isNotEmpty()) {
                onSuccess()
            } else {
                onFail()
            }
        },{
            onError()
        })
    }

    //A legutolsó kereskedett árat kérem le egy részvényhez, majd a paraméterben megadott függvényeket hívom vissza kimeneteltől függően
    @OptIn(DelicateCoroutinesApi::class)
    fun getLatestTrades(symbols: List<String>, onSuccess: (StockContainer) -> Unit, onError: () -> Unit) {
        GlobalScope.launch(Dispatchers.Main) {
            var innerException: Throwable? = null
            try {
                val response = client.get("https://data.alpaca.markets/v2/stocks/trades/latest") {
                    headers {
                        append("Apca-Api-Key-Id", keys.apiKey!!)
                        append("Apca-Api-Secret-Key", keys.apiSecret!!)
                    }
                    url {
                        parameters.append("symbols", symbols.joinToString(","))
                        parameters.append("feed", "iex")
                    }
                }


                if (response.status == HttpStatusCode.OK) {
                    val stocks : StockContainer = response.body()
                    try {
                        onSuccess(stocks)
                    } catch (e: Throwable) {
                        innerException = e
                    }
                } else {
                    println(response.bodyAsText())
                    throw Exception("API error")
                }
            } catch (e: Exception) {
                println(e.message)
                println(e.stackTrace)
                onError()
            } finally {
                if (innerException != null)
                    throw innerException
            }

        }
    }


    @OptIn(DelicateCoroutinesApi::class)
    fun getHistoricalPrices(symbol: String, start: String, end: String, timeFrame: String, onSuccess: (BarContainer) -> Unit, onError: () -> Unit) {

        GlobalScope.launch(Dispatchers.Main) {
            var innerException: Throwable? = null
            try {
                val response = client.get("https://data.alpaca.markets/v2/stocks/${symbol.uppercase()}/bars") {
                    headers {
                        append("Apca-Api-Key-Id", keys.apiKey!!)
                        append("Apca-Api-Secret-Key", keys.apiSecret!!)
                    }
                    url {
                        parameters.append("feed", "sip")
                        parameters.append("timeframe", timeFrame)
                        parameters.append("start", start)
                        parameters.append("end", end)
                        parameters.append("sort", "asc")
                        parameters.append("limit", 1000.toString())
                    }
                }


                if (response.status == HttpStatusCode.OK) {
                    val bars : BarContainer = response.body()

                    try {
                        onSuccess(bars)
                    } catch (e: Throwable) {
                        innerException = e
                    }



                } else {
                    println(response.bodyAsText())
                    throw Exception("Api Error")
                }
            } catch (e: Exception) {

                println(e.message)
                println(e.stackTrace)
                onError()

            } finally {
                if (innerException != null)
                    throw innerException
            }
        }
    }
}
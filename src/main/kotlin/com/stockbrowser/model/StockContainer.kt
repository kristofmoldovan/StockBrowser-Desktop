package com.stockbrowser.model

import com.fasterxml.jackson.annotation.JsonAnySetter
import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonProperty
import java.math.BigInteger
import java.util.*

//Api hívásokhoz használt osztály, ebben tárolok adatokat a különböző részvények utolsó kereskedéséről, vagyis az aktuális áráról
data class StockContainer(var trades: MutableMap<String, StockData> = mutableMapOf()) {

    data class StockData(
        @JsonProperty("c")
        var conditionFlags: List<String>? = null,
        @JsonProperty("i")
        var tradeId: BigInteger,
        @JsonProperty("p")
        var price: Double,
        @JsonProperty("s")
        var tradeSize: BigInteger,
        @JsonProperty("t")
        var time: Date,// String,
        @JsonProperty("x")
        var exchangeCode: String,
        @JsonProperty("z")
        var exchange: String
    ) {
        @JsonIgnore
        lateinit var name: String

        @JsonIgnore
        var favorite: Boolean = false
    }

    @JsonAnySetter
    fun add(key: String, value: StockData) {
        trades[key] = value
    }

    fun updateNames() {
        for (trade in trades) {
            trade.value.name = trade.key;
        }
    }
}
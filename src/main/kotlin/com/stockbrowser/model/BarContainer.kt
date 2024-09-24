package com.stockbrowser.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonSetter
import io.fair_acc.dataset.spi.financial.api.attrs.AttributeModel
import io.fair_acc.dataset.spi.financial.api.ohlcv.IOhlcv
import io.fair_acc.dataset.spi.financial.api.ohlcv.IOhlcvItem
import java.util.*

//Api hívásokhoz használt osztály, ebben tárolom egy részvény historikus árait
@JsonIgnoreProperties(ignoreUnknown = true)
class BarContainer (
    bars: MutableList<BarData>?
) : IOhlcv {

    @JsonProperty("bars")
    var bars: MutableList<BarData>

    init {
        this.bars = bars ?: mutableListOf()
    }

    @JsonSetter("bars")
    fun setBarsCustom(nullableBars: MutableList<BarData>?) {
        bars = nullableBars ?: mutableListOf()
    }

    class BarData(
        @JsonProperty("t")
        @JvmField
        //@JsonFormat(shape=JsonFormat.Shape.STRING, pattern="yyyy-MM-dd'T'HH:mm:ss.SSSZ", timezone="UTC")
        var timestamp: Date, //Timestamp
        @JsonProperty("o")
        @JvmField
        var open: Double, //Open
        @JsonProperty("h")
        @JvmField
        var high: Double, //High
        @JsonProperty("l")
        @JvmField
        var low: Double, //Low
        @JsonProperty("c")
        @JvmField
        var close: Double, //Close
        @JsonProperty("v")
        @JvmField
        var volume: Int, //Volume
        @JsonProperty("n")
        @JvmField
        var tradeCount: Int, //Trade Count
        @JvmField
        @JsonProperty("vw")
        var volumeWeightedPrice: Double //volume weighted average price
    ) : IOhlcvItem
    {
        override fun getTimeStamp() = timestamp

        override fun getOpen() = open

        override fun getHigh() = high

        override fun getLow() = low

        override fun getClose() = close

        override fun getVolume() = volume.toDouble()

        override fun getOpenInterest(): Double = 0.0

        override fun getAddon(): AttributeModel? = null

        override fun getAddonOrCreate(): AttributeModel? = null
    }

    override fun iterator()= bars.iterator()

    override fun getAddon(): AttributeModel {
        return AttributeModel()
    }

    override fun getAddonOrCreate(): AttributeModel {
        return AttributeModel()
    }

    override fun getOhlcvItem(index: Int) = bars[index]

    override fun size() = bars.size
}


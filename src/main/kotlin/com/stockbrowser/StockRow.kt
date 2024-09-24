package com.stockbrowser

import com.fasterxml.jackson.annotation.JsonIgnore
import javafx.beans.property.*

//Egy részvényt reprezentáló osztály a táblázatban
class StockRow {

    @JsonIgnore
    val symbolProperty: StringProperty = SimpleStringProperty(this, "symbol", "-")

    var symbol: String
        get() = symbolProperty.get()
        set(value) = symbolProperty.set(value)

    @JsonIgnore
    val priceProperty: StringProperty = SimpleStringProperty(this, "price", "-")

    @JsonIgnore
    var price: Double = 0.0
        set(value) {
            field = value


            priceProperty.set(if (value == 0.0) "-" else "$value $")
        }

    var favorite: Boolean

    constructor(symbol: String, price: Double, favorite: Boolean) {
        this.symbol = symbol
        this.price = price
        this.favorite = favorite
    }
}

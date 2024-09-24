package com.stockbrowser

import io.fair_acc.chartfx.XYChart
import io.fair_acc.chartfx.axes.AxisLabelOverlapPolicy
import io.fair_acc.chartfx.axes.spi.DefaultNumericAxis
import io.fair_acc.chartfx.plugins.DataPointTooltip
import io.fair_acc.chartfx.plugins.EditAxis
import io.fair_acc.chartfx.plugins.Zoomer
import io.fair_acc.chartfx.renderer.spi.financial.CandleStickRenderer
import io.fair_acc.dataset.spi.financial.OhlcvDataSet
import javafx.geometry.Pos
import javafx.scene.Scene
import javafx.scene.control.Button
import javafx.scene.layout.BorderPane
import javafx.scene.layout.HBox
import javafx.scene.layout.VBox
import javafx.stage.Modality
import javafx.stage.Stage
import java.text.SimpleDateFormat
import java.util.*


class Details(private var stock: StockRow, private val parent: Stage) : Stage() {

    private var chart: XYChart = XYChart(DefaultNumericAxis(), DefaultNumericAxis())
    private val candleStickRenderer = CandleStickRenderer()
    private val dataset = OhlcvDataSet(stock.symbol)


    private val vb = VBox()

    //Intervallum és Bar szélesség a grafikonhoz, API kérésekhez használom
    private lateinit var start: String
    private lateinit var end: String
    private lateinit var timeframe: String


    //Ablakméret
    companion object {
        private const val WIDTH = 1200
        private const val HEIGHT = 520
    }

    //GUI
    init {

        initModality(Modality.WINDOW_MODAL)
        initOwner(parent)
        isResizable = false

        x = parent.x + (parent.width / 2) - WIDTH / 2
        y = parent.y + (parent.height/ 2) - HEIGHT / 2

        val b24h = Button("24h")
        b24h.setOnAction {
            setInterval("24H")
        }
        val b5d = Button("5D")
        b5d.setOnAction {
            setInterval("5D")
        }
        val b30d = Button("30D")
        b30d.setOnAction {
            setInterval("30D")
        }
        val b6m = Button("6M")
        b6m.setOnAction {
            setInterval("6M")
        }
        val b1y = Button("1Y")
        b1y.setOnAction {
            setInterval("1Y")
        }
        val b5y = Button("5Y")
        b5y.setOnAction {
            setInterval("5Y")
        }

        val hb = HBox()
        hb.children.add(b24h)
        hb.children.add(b5d)
        hb.children.add(b30d)
        hb.children.add(b6m)
        hb.children.add(b1y)
        hb.children.add(b5y)

        hb.alignment = Pos.CENTER



        vb.children.add(hb)
        vb.children.add(chart)

        setInterval("24H")

        val r = BorderPane()
        r.center = vb
        scene = Scene(r, WIDTH.toDouble(), HEIGHT.toDouble())



    }

    //Intervallum beállítása gombok alapján
    private fun setInterval(type: String) {
        val cal = Calendar.getInstance()

        val now = Date()

        cal.timeZone = TimeZone.getTimeZone("Europe/Budapest")

        //FORMATTER
        val formatter = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX")
        formatter.timeZone = TimeZone.getTimeZone("UTC")


        //MOST
        cal.time = now


        //API korlátozások miatt 15 percet levonok az élő időpontból
        cal.add(Calendar.MINUTE, -15)
        end = formatter.format(cal.time)

        //KEZDŐDÁTUM BEÁLLÍTÁSA
        cal.time = now
        when (type) {
            "5D" -> {
                cal.add(Calendar.DATE, -5)
                timeframe = "15Min"
            }

            "30D" -> {
                cal.add(Calendar.DATE, -30)
                timeframe = "1Hour"
            }

            "6M" -> {
                cal.add(Calendar.MONTH, -6)
                timeframe = "5Hour"
            }

            "1Y" -> {
                cal.add(Calendar.YEAR, -1)
                timeframe = "12Hour"
            }

            "5Y" -> {
                cal.add(Calendar.YEAR, -5)
                timeframe = "1Week"
            }

            //24h
            else -> {
                cal.add(Calendar.DATE, -1)
                timeframe = "4Min"
            }

        }

        start = formatter.format(cal.time)
        buildChart()


    }

    //Adatok letöltése és grafikon megjelenítése
    private fun buildChart() {
        println("Adatok lekérése: ${stock.symbol} $start - $end - $timeframe")
        NetworkManager.getHistoricalPrices(stock.symbol, start,  end, timeframe, {
            println("Adatok lekérve: ${stock.symbol} $start - $end - $timeframe")



            val xAxis = DefaultNumericAxis("UTC time", "iso")
            val yAxis = DefaultNumericAxis("price", "$")

            chart = XYChart(xAxis, yAxis)



            chart.renderers.clear()
            chart.renderers.add(candleStickRenderer)


            chart.plugins.add(Zoomer())
            chart.plugins.add(EditAxis())
            chart.plugins.add(DataPointTooltip())

            xAxis.overlapPolicy = AxisLabelOverlapPolicy.SKIP_ALT

            xAxis.isTimeAxis = true

            yAxis.isAutoRangeRounding = true
            chart.yAxis.isAutoRanging = true


            chart.isAnimated = false


            chart.yAxis.isAutoRanging = true


            val ds = OhlcvDataSet(stock.symbol)
            ds.data = it
            dataset.set(ds)

            chart.datasets.clear()
            chart.datasets.addAll(ds)

            chart.autosize()

            vb.children[vb.children.size - 1] = chart


        }, {
            println("Couldn't download data.")
        })

    }

}
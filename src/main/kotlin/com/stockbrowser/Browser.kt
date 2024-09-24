package com.stockbrowser

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import javafx.application.Application
import javafx.collections.FXCollections
import javafx.collections.ObservableList
import javafx.collections.transformation.FilteredList
import javafx.event.EventHandler
import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.Scene
import javafx.scene.control.*
import javafx.scene.layout.BorderPane
import javafx.scene.layout.HBox
import javafx.scene.layout.VBox
import javafx.stage.Stage
import javafx.util.Callback
import java.io.File
import java.nio.file.Paths


class Browser : Application() {


    private lateinit var keyField: TextField
    private lateinit var secretField: TextField

    private val apiMsgLabel = Label()

    private val keyButton = Button("Loading and checking api keys...")


    private var auth = Keys(null, null)

    private var keyNodes = mutableListOf<Control>()
    private var otherControlNodes = mutableListOf<Control>()

    private var authSetState = false

    private lateinit var mainTable: TableView<StockRow>
    private lateinit var stockList: ObservableList<StockRow>
    private lateinit var filteredWrapper: FilteredList<StockRow>

    companion object {
        private const val WIDTH = 800
        private const val HEIGHT = 550
    }

    //GUI beállítások
    override fun start(mainStage: Stage) {



        mainStage.title = "Stock Browser"
        println("Save location: ${Paths.get("").toAbsolutePath()}")
        loadStocks()



        //GUI

        val keyLabel = Label("Api key: ")
        keyField = TextField()
        val secretLabel = Label("Api secret: ")
        secretField = TextField()



        keyButton.setOnAction {

            setState(State.WAITING)
            if (authSetState) {
                //API KEY CHECK
                val inputKeys = Keys(keyField.text, secretField.text)


                apiMsgLabel.text = "Checking api keys..."
                NetworkManager.checkApiKeys(inputKeys, {
                    setState(State.USE)
                    this.auth = inputKeys
                    NetworkManager.keys = auth
                    saveApiKeys()
                    apiMsgLabel.text = ""
                    authSetState = !authSetState
                }, {
                    setState(State.API_KEYS)
                    apiMsgLabel.text = "Wrong api keys or network error."
                })


            } else {
                apiMsgLabel.text = ""
                setState(State.API_KEYS)
                authSetState = !authSetState
            }

        }

        val filterLabel = Label("Show only favorites")
        val filterCB = CheckBox()


        val addSymbolButton = Button("Add symbol")
        val updateButton = Button("Update prices")
        updateButton.setOnAction {
            println("Requesting prices...")

            getPrices()
        }

        addSymbolButton.onAction =
            EventHandler {
                val td = TextInputDialog()
                td.title = "Add symbol"
                td.contentText = "Add symbol"
                val r = td.showAndWait()
                if (r.isPresent)
                    addStock(r.get())
            }

        filterCB.setOnAction {
            if (filterCB.isSelected) {
                println("Filter: on")
                filteredWrapper.setPredicate { row -> row.favorite }
            } else {
                println("Filter: off")
                filteredWrapper.setPredicate { _ -> true }
            }
        }

        createTable(mainStage)

        val tableBox = VBox()
        tableBox.children.add(mainTable)
        tableBox.padding = Insets(0.0, 20.0, 0.0, 20.0)

        //VBOX
        val vb = VBox()


        val apiSettings = VBox()
        apiSettings.padding = Insets(0.0, 20.0, 0.0, 20.0)
        val apiInputHB = HBox()
        val keyVB = VBox()
        val secretVB = VBox()
        keyVB.children.add(keyLabel)
        keyVB.children.add(keyField)
        secretVB.children.add(secretLabel)
        secretVB.children.add(secretField)


        apiInputHB.children.add(keyVB)
        apiInputHB.children.add(secretVB)


        apiSettings.children.add(apiInputHB)

        val saveApiHB = HBox()
        saveApiHB.children.add(keyButton)
        saveApiHB.children.add(apiMsgLabel)
        saveApiHB.spacing = 10.0


        apiSettings.children.add(saveApiHB)

        apiInputHB.spacing = 10.0
        apiSettings.spacing = 5.0

        val filterHb = HBox()

        filterHb.padding = Insets(10.0, 0.0, 10.0, 20.0)
        filterHb.spacing = 2.0

        filterHb.children.add(filterLabel)
        filterHb.children.add(filterCB)

        val toolsHb = HBox()
        toolsHb.children.add(addSymbolButton)
        toolsHb.children.add(updateButton)
        toolsHb.alignment = Pos.CENTER_RIGHT

        toolsHb.spacing = 5.0
        toolsHb.padding = Insets(5.0, 20.0, 0.0, 20.0)

        vb.children.add(apiSettings)
        vb.children.add(filterHb)

        vb.children.add(tableBox)
        vb.children.add(toolsHb)

        //Kategóriák
        //keys
        keyNodes.add(keyField)
        keyNodes.add(secretField)

        //not key nodes
        otherControlNodes.add(addSymbolButton)
        otherControlNodes.add(updateButton)
        otherControlNodes.add(mainTable)
        otherControlNodes.add(filterCB)


        // create a stack pane
        val r = BorderPane()
        r.center = vb

        val scene = Scene(r, WIDTH.toDouble(), HEIGHT.toDouble())

        mainStage.scene = scene

        setState(State.WAITING)
        loadApiKeys(true)


        mainStage.show()
    }

    //Hozzáadok egy részvényt a listához, amennyiben az létezik
    private fun addStock(symbol: String) {
        val s = symbol.uppercase()

        setState(State.WAITING)

        NetworkManager.checkIfSymbolExists(s, {
            println("$symbol exists")
            stockList.add(StockRow(s, 0.0, false))
            saveStocks()
            getPrices()
            setState(State.USE)
        },{
            println("$symbol doesn't exist")
            setState(State.USE)
        }, {
            setState(State.USE)
        })



    }

    //Betöltöm a részvényeket a táblázatba, mentési fájlból
    private fun loadStocks() {
        try {
            val mapper = jacksonObjectMapper()
            val text = File("stocks.json").readText()
            val tempList = mapper.readValue<MutableList<StockRow>>(text)

            this.stockList = FXCollections.observableList(tempList) //ObservableList<StockRow>()
        } catch (e: Exception) {
            println(e.message)
            println(e)
            println(e.stackTrace)

            this.stockList = FXCollections.observableList(mutableListOf<StockRow>())

            println("Couldn't load stocks from savefile, created an empty list instead")
        }
    }

    //Elmentem fájlba a táblázatban található részvényeket
    private fun saveStocks() {
        val mapper = jacksonObjectMapper()
        val serialized = mapper.writeValueAsString(stockList as MutableList<StockRow>)
        try {
            File("stocks.json").writeText(serialized)
        } catch (e: Exception) {
            println(e.message)
            println(e.stackTrace)
            println("Failed to save stocks.")
        }
    }

    //Frissítem az árakat a táblázatban. Ha valamilyen okból kifolyólag nem létezik az adott részvény, akkor 0 -lesz az ára.
    private fun getPrices() {

        if (mainTable.items.size <= 0)
            return

        val symbols = mainTable.items.map { it.symbol }
        setState(State.WAITING)
        NetworkManager.getLatestTrades(symbols ,
            {
                for (item in mainTable.items) {
                   item.price = it.trades[item.symbol.uppercase()]?.price ?: 0.0
                }
                setState(State.USE)
            }, {
                setState(State.USE)
            })
    }


    //Beállítom, hogy milyen állapotban van az alkalmazás (API kulcs beállítása, Várakozás vagy Böngészés)
    private fun setState(state: State) {
        when (state) {
            State.API_KEYS -> {
                otherControlNodes.forEach { node -> node.isDisable = true }
                keyNodes.forEach { node -> node.isDisable = false }
                keyButton.text = "Save API keys"
            }
            State.USE -> {
                otherControlNodes.forEach { node -> node.isDisable = false }
                keyNodes.forEach { node -> node.isDisable = true }
                keyButton.text = "Change API keys"
            }
            State.WAITING -> {
                otherControlNodes.forEach { node -> node.isDisable = true }
                keyNodes.forEach { node -> node.isDisable = true }
            }
        }
    }

    //Betöltöm az api kulcsokat, ha a "thenGetPrices" paraméter true, akkor az árakat is frissítem utána
    private fun loadApiKeys(thenGetPrices: Boolean = false) {
        setState(State.WAITING)
        try {
            val mapper = jacksonObjectMapper()
            val text = File("keys.json").readText()
            val readKeys = mapper.readValue<Keys>(text)

            if (readKeys.apiKey?.isNotBlank() == true && readKeys.apiSecret?.isNotBlank() == true)
                NetworkManager.checkApiKeys(readKeys, {
                    auth = readKeys
                    keyField.text = auth.apiKey
                    secretField.text = auth.apiSecret
                    authSetState = false



                    NetworkManager.keys = auth

                    if (thenGetPrices)
                        getPrices()

                    setState(State.USE)

                    }, {
                    keyButton.text = "Save API keys"
                    keyField.text = auth.apiKey
                    secretField.text = auth.apiSecret
                    authSetState = true
                    setState(State.API_KEYS)
                    })
            else {
                keyField.text = auth.apiKey
                secretField.text = auth.apiSecret
                keyButton.text = "Change API keys"
                authSetState = true
                setState(State.API_KEYS)
            }


        } catch (e: Exception) {
            println("Couldnt load API keys, asking user instead.")
            authSetState = true
            setState(State.API_KEYS)
            keyButton.text = "Save API keys"
            println(e.stackTrace)
        }



    }

    //Elmentem az api kulcsokat fájlba.
    private fun saveApiKeys() {
        val mapper = jacksonObjectMapper()
        val serialized = mapper.writeValueAsString(auth)
        try {

        } catch (e: Exception) {
            println(e)
            e.printStackTrace()
            println("Couldn't save api keys to file.")
        }
        File("keys.json").writeText(serialized)
    }

    //Beállítom a táblázatot
    private fun createTable(mainStage: Stage) {
        this.mainTable = TableView<StockRow>()

        //COLUMNS
        val nameCol = TableColumn<StockRow, String>("Symbol")
        nameCol.setCellValueFactory { it.value.symbolProperty }
        mainTable.columns += nameCol



        val priceCol = TableColumn<StockRow, String>("Price")
        priceCol.setCellValueFactory { it.value.priceProperty}
        mainTable.columns += priceCol


        val favoriteCol = TableColumn<StockRow?, String?>("Favorite")

        val favCellFactory: Callback<TableColumn<StockRow?, String?>?, TableCell<StockRow?, String?>?> =  //
            Callback<TableColumn<StockRow?, String?>?, TableCell<StockRow?, String?>?> {
                object : TableCell<StockRow?, String?>() {
                    var cb = CheckBox()
                    override fun updateItem(item: String?, empty: Boolean) {
                        super.updateItem(item, empty)

                        alignment = Pos.CENTER

                        if (empty) {
                            graphic = null
                            text = null
                        } else {
                            cb.isSelected = mainTable.items[index].favorite
                            cb.setOnAction {
                                val stock: StockRow = mainTable.items[index]

                                if (cb.isSelected) {
                                    println(
                                        "Kedvencelted: ${stock.symbol}"
                                    )
                                    stock.favorite = true



                                } else {
                                    println("Kivetted a kedvencek közül: ${stock.symbol}")
                                    stock.favorite = false
                                }
                                stockList.sortByDescending { it.favorite }
                                saveStocks()

                            }
                            graphic = cb
                            text = null
                        }
                    }
                }
            }

        favoriteCol.cellFactory = favCellFactory
        mainTable.columns += favoriteCol


        val actionCol = TableColumn<StockRow?, String?>("Historical prices")

        val cellFactory: Callback<TableColumn<StockRow?, String?>?, TableCell<StockRow?, String?>?> =  //
            Callback<TableColumn<StockRow?, String?>?, TableCell<StockRow?, String?>?> {
                object : TableCell<StockRow?, String?>() {
                    val btn = Button("Show historical prices")
                    override fun updateItem(item: String?, empty: Boolean) {
                        super.updateItem(item, empty)

                        alignment = Pos.CENTER

                        if (empty) {
                            graphic = null
                            text = null
                        } else {
                            btn.setOnAction {
                                val stock: StockRow = mainTable.items[index]
                                println(
                                    "Megtekintetted: ${stock.symbol}"
                                )

                                Details(stock, mainStage).show()
                            }
                            graphic = btn
                            text = null
                        }
                    }
                }
            }

        actionCol.cellFactory = cellFactory
        mainTable.columns += actionCol

        val removeCol = TableColumn<StockRow?, String?>("Remove stock")

        val removeCellFactory: Callback<TableColumn<StockRow?, String?>?, TableCell<StockRow?, String?>?> =  //
            Callback<TableColumn<StockRow?, String?>?, TableCell<StockRow?, String?>?> {
                object : TableCell<StockRow?, String?>() {
                    val btn = Button("X")
                    override fun updateItem(item: String?, empty: Boolean) {
                        super.updateItem(item, empty)

                        alignment = Pos.CENTER

                        if (empty) {
                            graphic = null
                            text = null
                        } else {
                            btn.setOnAction {
                                val stock: StockRow = mainTable.items[index]
                                println(
                                    "Törölted: ${stock.symbol}"
                                )
                                stockList.remove(stock)
                                saveStocks()
                            }
                            graphic = btn
                            text = null
                        }
                    }
                }
            }

        removeCol.cellFactory = removeCellFactory
        mainTable.columns += removeCol

        nameCol.prefWidthProperty().bind(mainTable.widthProperty().multiply(0.2))
        priceCol.prefWidthProperty().bind(mainTable.widthProperty().multiply(0.15))
        favoriteCol.prefWidthProperty().bind(mainTable.widthProperty().multiply(0.15))
        actionCol.prefWidthProperty().bind(mainTable.widthProperty().multiply(0.3))
        removeCol.prefWidthProperty().bind(mainTable.widthProperty().multiply(0.19))

        nameCol.style += "-fx-alignment: CENTER;"
        priceCol.style += "-fx-alignment: CENTER;"
        //sizeCol.setStyle("-fx-alignment: CENTER-RIGHT;");

        nameCol.isResizable = false
        priceCol.isResizable = false
        favoriteCol.isResizable = false
        actionCol.isResizable = false
        removeCol.isResizable = false

        //Szűrhetőség
        this.filteredWrapper = FilteredList(stockList)

        mainTable.items = filteredWrapper
    }

}
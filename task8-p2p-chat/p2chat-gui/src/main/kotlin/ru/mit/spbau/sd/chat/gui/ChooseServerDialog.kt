package ru.mit.spbau.sd.chat.gui

import javafx.beans.property.SimpleIntegerProperty
import javafx.beans.property.SimpleStringProperty
import javafx.scene.input.KeyCode
import tornadofx.*


/**
 * Peer server settings edit dialog
 */
class ChooseServerDialog(host: String, port: Int): Fragment() {
    override val root = Form()
    val serverAddrProp = SimpleStringProperty(host)
    val serverPort = SimpleIntegerProperty(port)

    init {
        title = "Change server peer address"

        with (root) {
            prefHeight = 200.0
            prefWidth = 300.0
            fieldset("Peer-server settings") {
                field("address") {
                    textfield().bind(serverAddrProp)
                }
                field("port") {
                    textfield().bind(serverPort)
                }
            }

            setOnKeyPressed { if (it.code == KeyCode.ENTER) setServer() }
            button("Save") {
                setOnAction {
                    setServer()
                }
                disableProperty().bind(serverAddrProp.isNull)
            }
        }
    }

    fun setServer() {
        fire(NewPeerServerChosenEvent(serverAddrProp.get(), serverPort.get()))
        closeModal()
    }
}
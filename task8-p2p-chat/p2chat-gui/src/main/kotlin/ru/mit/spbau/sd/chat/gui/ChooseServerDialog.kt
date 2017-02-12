package ru.mit.spbau.sd.chat.gui

import javafx.beans.property.SimpleIntegerProperty
import javafx.beans.property.SimpleStringProperty
import tornadofx.*


/**
 * Server settings edit dialog
 */
class ChooseServerDialog(host: String, port: Int): Fragment() {
    override val root = Form()
    val serverAddrProp = SimpleStringProperty(host)
    val serverPort = SimpleIntegerProperty(port)

    init {
        title = "Change server address"

        with (root) {
            fieldset("Server settings") {
                field("address") {
                    textfield().bind(serverAddrProp)
                }
                field("port") {
                    textfield().bind(serverPort)
                }
            }

            button("Save") {
                setOnAction {
                    fire(NewServerChosenEvent(serverAddrProp.get(), serverPort.get()))
                    closeModal()
                }
                disableProperty().bind(serverAddrProp.isNull)
            }
        }
    }
}
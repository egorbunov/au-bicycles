package ru.mit.spbau.sd.chat.gui

import javafx.beans.property.SimpleIntegerProperty
import javafx.beans.property.SimpleStringProperty
import javafx.scene.input.KeyCode
import javafx.scene.input.KeyEvent
import javafx.scene.layout.BorderPane
import tornadofx.*


/**
 * Peer server settings edit dialog
 */
class ChooseServerDialog(host: String, port: Int, private val controller: ChatUIController) : View() {
    override val root = BorderPane()
    val serverAddrProp = SimpleStringProperty(host)
    val serverPort = SimpleIntegerProperty(port)

    init {
        title = "Change server peer address"

        val btnSave = button("Save") {
            setOnAction {
                controller.newPeerServerChosen(serverAddrProp.get(), serverPort.get())
                closeModal()
            }
            disableProperty().bind(serverAddrProp.isNull)
        }

        with(root) {
            center {
                setOnKeyPressed {
                    if (it.code == KeyCode.ENTER && !btnSave.isDisabled) {
                        controller.newPeerServerChosen(serverAddrProp.get(), serverPort.get())
                        closeModal()
                    }
                }

                form {
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
                    add(btnSave)
                }
            }

        }
    }
}

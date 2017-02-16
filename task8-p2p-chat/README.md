# P2P Chat

## Build

1. Clone project and `cd` to it's root directory and when to `task8-p2p-chat` dir.
2. Run 
```bash
[user task8-p2p-chat]$ ./gradlew build copyJars
```
3. Jars will be generated and copied to `build/jars` directory


## How to

Now you can go to `build/jars` and:

To run client you simple want to execute: `java -jar p2chat-gui-client.jar`. You will see chat client window,
which is devided into 4 parts:

* Menu bar, from which you can go to client personal settings (actually, you can only change client name and
see your client address); 
server settings, where you can set chat peer-server address (after submitting the form chat will try to connect)
* Available users for chatting, whom reside on the right part of the pane
* Text field to write messages and part for messages view

To start chatting you have to choose one of available users from the right part of the pane! You also
can chat with yourself =)

## About

* Asynchronous network interaction only (almost)
* Kotlin + JavaFX: https://github.com/edvin/tornadofx


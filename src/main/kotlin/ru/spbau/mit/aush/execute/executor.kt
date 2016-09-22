package ru.spbau.mit.aush.execute

/**
 * Created by: Egor Gorbunov
 * Date: 9/23/16
 * Email: egor-mailbox@ya.com
 */

/*
    Executor takes statement and match it's type.
    If statement is simple command, then executor
    looks for proper executing procedure among classes,
    which implement Cmd interface. That is done with
    reflections library:
    ```
        val r = Reflections(ConfigurationBuilder()
            .setScanners(SubTypesScanner())
            .setUrls(ClasspathHelper.forPackage("ru.spbau.mit")))
    ```
 */
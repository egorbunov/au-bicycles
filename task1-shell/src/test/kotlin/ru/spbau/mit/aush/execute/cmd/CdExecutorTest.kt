package ru.spbau.mit.aush.execute.cmd

import org.junit.Assert
import org.junit.Test

import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import ru.spbau.mit.aush.execute.AushContext
import ru.spbau.mit.aush.execute.SpecialVars
import ru.spbau.mit.aush.execute.error.BadCmdArgsError
import java.io.File
import java.util.*

/**
 * @author Anton Mordberg
 * *
 * @since 10.02.17
 */
class CdExecutorTest {

    @Rule
    @JvmField val tesFolder = TemporaryFolder()

    private var executor: CmdExecutor? = null

    @Before
    fun setUp() {
        /*
          - folder
            * file
            - subfolder
         */
        tesFolder.newFile("TEST_FILE.txt")
        tesFolder.newFolder("TEST_FOLDER")

        AushContext.instance.addVar(SpecialVars.PWD, tesFolder.root.absolutePath)

        executor = CdExecutor()
    }

    @Test(expected = BadCmdArgsError::class)
    fun execTooManyArgs() {
        executor!!.exec(listOf("1", "2"), System.`in`, System.out)
    }

    @Test(expected = BadCmdArgsError::class)
    fun execNoArgs() {
        executor!!.exec(emptyList(), System.`in`, System.out)
    }

    @Test(expected = BadCmdArgsError::class)
    fun execNotDir() {
        executor!!.exec(Collections.singletonList("TEST_FILE.txt"), System.`in`, System.out)
    }

    @Test
    fun exec() {
        val oldDir = AushContext.instance.getVar(SpecialVars.PWD)
        Assert.assertEquals(0, executor!!.exec(Collections.singletonList("TEST_FOLDER"), System.`in`, System.out))
        val newDir = AushContext.instance.getVar(SpecialVars.PWD)
        Assert.assertEquals(oldDir + "/TEST_FOLDER", newDir)
    }

}
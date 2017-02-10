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
import java.io.ByteArrayOutputStream
import java.io.File
import java.util.*

/**
 * @author Anton Mordberg
 * *
 * @since 10.02.17
 */
class LsExecutorTest {

    @Rule
    @JvmField val testFolder = TemporaryFolder()

    private var executor: CmdExecutor? = null

    private val filename = "TEST_FILE.txt"
    private val foldername = "TEST_FOLDER"
    private var testFile: File? = null
    private var testSubfolder: File? = null

    @Before
    fun setUp() {
        /*
          - folder
            * file
            - subfolder
         */
        testFile = testFolder.newFile(filename)
        testSubfolder = testFolder.newFolder(foldername)

        AushContext.instance.addVar(SpecialVars.PWD, testFolder.root.absolutePath)

        executor = LsExecutor()
    }

    @Test(expected = BadCmdArgsError::class)
    fun execNotExisting() {
        executor!!.exec(Collections.singletonList("not existing file"), System.`in`, System.out)
    }

    @Test(expected = BadCmdArgsError::class)
    fun execTooManyArgs() {
        executor!!.exec(listOf("1", "2"), System.`in`, System.out)
    }

    @Test
    fun execNoArgs() {
        val out = ByteArrayOutputStream()
        Assert.assertEquals(0, executor!!.exec(emptyList(), System.`in`, out))
        val actual = String(out.toByteArray())

        val expected = listOf(filename, foldername).sorted().joinToString("\n") + "\n"

        Assert.assertEquals(expected, actual)
    }

    @Test
    fun execFile() {
        val out = ByteArrayOutputStream()
        Assert.assertEquals(0, executor!!.exec(Collections.singletonList(testFile!!.absolutePath), System.`in`, out))
        val actual = String(out.toByteArray())

        val expected = testFile!!.absolutePath + "\n"

        Assert.assertEquals(expected, actual)
    }
}
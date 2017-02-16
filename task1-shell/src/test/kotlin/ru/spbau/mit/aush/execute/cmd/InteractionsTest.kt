package ru.spbau.mit.aush.execute.cmd

import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import ru.spbau.mit.aush.execute.AushContext
import ru.spbau.mit.aush.execute.SpecialVars
import java.io.ByteArrayOutputStream
import java.io.File
import java.nio.file.Paths
import java.util.*
import java.util.stream.Collectors

/**
 * @author Anton Mordberg
 * @since 16.02.17
 */
class InteractionsTest {

    @Rule
    @JvmField val folder = TemporaryFolder()
    private var subfolder: File? = null
    private var subsubfolder1: File? = null
    private var subsubfolder2: File? = null

    private var lsExecutor: LsExecutor? = null
    private var pwdExecutor: PWDExecutor? = null
    private var cdExecutor: CdExecutor? = null

    @Before
    fun setUp() {
        /*
          - folder
            - subfolder
              - subsubfolder1
              - subsubfolder2
         */

        subfolder = folder.newFolder("subfolder")
        subsubfolder1 = folder.newFolder("subfolder/subsubfolder1")
        subsubfolder2 = folder.newFolder("subfolder/subsubfolder2")

        AushContext.instance.addVar(SpecialVars.PWD, folder.root.absolutePath)

        cdExecutor = CdExecutor()
        lsExecutor = LsExecutor()
        pwdExecutor = PWDExecutor()
    }

    @Test
    fun execCdAndLs() {
        checkLs("subfolder")
        cdExecutor!!.exec(Collections.singletonList("subfolder"), System.`in`, System.out)
        checkLs("subsubfolder1", "subsubfolder2")
    }

    @Test
    fun execCdAndPwd() {
        val rootPath = getPwd()
        cdExecutor!!.exec(Collections.singletonList("subfolder"), System.`in`, System.out)
        val newPath = getPwd()

        Assert.assertEquals(Paths.get(rootPath).toAbsolutePath(), Paths.get(newPath).parent.toAbsolutePath())
    }

    fun checkLs(vararg list: String) {
        val out = ByteArrayOutputStream()
        lsExecutor!!.exec(emptyList(), System.`in`, out)
        val actual = String(out.toByteArray())
        val expected = Arrays.stream(list).sorted().collect(Collectors.toList()).joinToString("\n") + "\n"

        Assert.assertEquals(expected, actual)
    }

    fun getPwd(): String {
        val out = ByteArrayOutputStream()
        pwdExecutor!!.exec(emptyList(), System.`in`, out)
        return String(out.toByteArray()).trim()
    }
}

package dev.iptvplayer.tv.ui.settings

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class LicensesTest {
    private fun asset(name: String) = File("src/main/assets/licenses/$name")

    @Test fun everyLicenseThePageShowsIsPackaged() {
        licenseTexts.forEach { assertTrue(it.file, asset(it.file).isFile) }
    }

    @Test fun theLicensesAreWhole() {
        val apache = asset("apache-2.0.txt").readText()
        assertTrue(apache.contains("TERMS AND CONDITIONS FOR USE, REPRODUCTION, AND DISTRIBUTION"))
        assertTrue(apache.contains("END OF TERMS AND CONDITIONS"))
        assertTrue(asset("mit-qrcodegen.txt").readText().contains("Copyright (c) Project Nayuki"))
        assertTrue(asset("roboto-ofl.txt").readText().contains("SIL OPEN FONT LICENSE"))
    }

    @Test fun hardBrokenLinesBecomeParagraphsAndListsKeepTheirLines() {
        val text = "Apache License\r\n   Version 2.0\n\n---------\nFirst line\n  goes on.\n\n\nSecond:\n- one\n  more\n- two\n(a) three\n"
        assertEquals("Apache License Version 2.0\n\nFirst line goes on.\n\nSecond:\n- one more\n- two\n(a) three", reflowLicense(text))
    }
}

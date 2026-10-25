package com.example

import android.app.Application
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("MC Mobile", appName)
    }

    @Test
    fun `verify companion view model initialization`() {
        val app = ApplicationProvider.getApplicationContext<Application>()
        val vm = CompanionViewModel(app)

        assertEquals("patch", vm.selectedMode.value)
        assertTrue(vm.hudModules.value.isNotEmpty())
        assertTrue(vm.servers.value.isNotEmpty())

        val json = vm.exportConfigJson()
        assertNotNull(json)
        assertTrue(json.contains("modules"))
        assertTrue(json.contains("crosshair"))
    }
}

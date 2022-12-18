package info.hannes.github

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AppUpdateHelperTest {

    @org.junit.Before
    fun setUp() {
    }

    @org.junit.After
    fun tearDown() {
    }

    @Test
    fun getPackageInfo() {
    }

    @Test
    fun requestArtifactoryVersions() = runTest {
        val data = AppUpdateHelper.requestArtifactoryVersions("https://artifactory.mxtracks.info")
        assertNull(data)
    }

    @Test
    fun dataShouldBeHelloWorld() = runTest {
        val data = fetchData()
        assertEquals("Hello world", data)
    }

    suspend fun fetchData(): String {
        delay(1000L)
        return "Hello world"
    }

}
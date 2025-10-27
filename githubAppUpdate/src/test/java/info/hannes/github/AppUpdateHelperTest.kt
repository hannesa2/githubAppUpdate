package info.hannes.github

import junit.framework.Assert.assertEquals
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.runTest
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AppUpdateHelperTest {

    @org.junit.Before
    fun setUp() {
    }

    @org.junit.After
    fun tearDown() {
    }

    @org.junit.Test
    fun getPackageInfo() {
    }

    @org.junit.Test
    fun requestArtifactoryVersions() = runTest {
        val data = AppUpdateHelper.requestArtifactoryVersions("https://artifactory.mxtracks.info")
        assertEquals("Hello world", data)
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
package com.smackwerks.kotlinchannels

import com.smackwerks.kotlinchannels.data.RepoModel
import junit.framework.Assert.assertNotNull
import kotlinx.coroutines.experimental.runBlocking
import org.junit.Test
import timber.log.Timber

class RepoModelTest {
    @Test
    fun getKotlinTitles() {
        val model = RepoModel()
        runBlocking {
            val chan = model.getRepos()
            Timber.d("Trying receive")
            val first = chan.receiveOrNull()
            Timber.d("Receive completed")
            assertNotNull(first)
        }
    }
}
package com.smackwerks.kotlinchannels

import com.smackwerks.kotlinchannels.data.RepoModel.getRepos
import junit.framework.Assert.assertNotNull
import junit.framework.Assert.assertNull
import kotlinx.coroutines.experimental.delay
import kotlinx.coroutines.experimental.runBlocking
import org.junit.Test

class RepoModelTest {
    @Test
    fun testLookaheadAndClosing() {
        runBlocking {
            val buffer = 10  // This should stay less than the # of repos Github returns
            val chan = getRepos(lookAhead = buffer)
            val first = chan.receiveOrNull()
            assertNotNull(first)

            delay(1000) // Ensure model has time to fill the channel
            chan.close()
            delay(1000)
            assertNotNull(chan.receiveOrNull()) // We should still have elements in the channel after a close

            // Note, the model was has a pending send that was blocked so this replaces the last receive
            repeat(buffer) { chan.receiveOrNull() }  // Empty out the channel; Model should not keep writing to closed channel
            assertNull(chan.receiveOrNull())
        }
    }

    @Test
    fun testKotlinMatches() {

        runBlocking {

        }
    }
}
import org.junit.jupiter.api.Test
import java.util.concurrent.CountDownLatch
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger
import kotlin.concurrent.thread
import kotlin.random.Random


/**
 * MIT License
 *
 * Copyright (c) 2024 Alexander Koch
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

internal class LockManagerTest {

    @Test
    fun lock() {
        val lockito = Lockito<Long>()
        val lockObject = Random.nextLong()

        lockito.lock(lockObject)
        assert(lockito.isLocked(lockObject))

        lockito.unlock(lockObject)
        assert(!lockito.isLocked(lockObject))
    }

    @Test
    fun lock_withRunnable() {
        val lockito = Lockito<Long>()
        val lockObject = Random.nextLong()

        val workDone = AtomicBoolean(false)

        lockito.lock(lockObject) {
            workDone.set(true)
        }
        assert(!lockito.isLocked(lockObject))
        assert(workDone.get())
    }

    @Test
    fun testConcurrentAccess() {
        val lockito = Lockito<String>()
        val lockObject = "testObject"
        val countDownLatch = CountDownLatch(1)

        val workDone = AtomicInteger(0)

        val thread1 = thread {
            lockito.lock(lockObject)
            // now thread 2 can run - we have the lock
            countDownLatch.countDown()
            workDone.incrementAndGet()
            lockito.unlock(lockObject)
        }

        val thread2 = thread {
            // wait for thread 1 to acquire the lock
            countDownLatch.await()
            // wait for thread 1 to release the lock
            lockito.lock(lockObject)
            assert(workDone.get() == 1)
            workDone.incrementAndGet()
            lockito.unlock(lockObject)
        }

        thread1.join()
        thread2.join()

        assert(workDone.get() == 2)
    }

    @Test
    fun lock_withSupplier() {
        val lockito = Lockito<Long>()
        val lockObject = Random.nextLong()

        val workDone = AtomicBoolean(false)

        val result = lockito.lock(lockObject) {
            workDone.set(Random.nextBoolean())
            workDone.get()
        }
        assert(!lockito.isLocked(lockObject))
        assert(workDone.get() == result)
    }

}
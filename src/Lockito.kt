import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.locks.ReentrantLock
import java.util.function.Supplier

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

class Lockito<T> {

    private val lockManager = LockManager<T>()

    fun lock(lockObject: T) = lockManager.lock(lockObject)
    fun isLocked(lockObject: T): Boolean = lockManager.isLocked(lockObject)
    fun unlock(lockObject: T) = lockManager.unlock(lockObject)

    fun <R> lock(lockObject: T, supplier: Supplier<R>):R {
        var exception: Exception? = null
        var result: R? = null

        lockManager.lock(lockObject)
        try {
            result = supplier.get()
        } catch (e: Exception) {
            exception = e
        }finally {
            unlock(lockObject)
        }
        if(exception != null) throw exception
        return result!!
    }

}

class LockManager<T> {

    private val locks = ConcurrentHashMap<T, ReentrantLock>()

    fun lock(lockObject: T) {
        val lock = locks.computeIfAbsent(lockObject) { ReentrantLock() }
        lock.lock()
    }

    fun isLocked(lockObject: T): Boolean {
        return locks.computeIfAbsent(lockObject) { ReentrantLock() }.isLocked
    }

    fun unlock(lockObject: T) {
        val lock = locks.computeIfAbsent(lockObject) { ReentrantLock() }
        lock.unlock()
        if (!lock.hasQueuedThreads()) {
            locks.remove(lockObject)
        }
    }

}

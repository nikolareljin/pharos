package io.github.nikolareljin.pharos.core.identity

import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.UUID
import java.util.concurrent.atomic.AtomicInteger

class NodeIdentityTest {

    private class FakeStore(var stored: String? = null) : NodeIdentityStore {
        val writes = AtomicInteger(0)
        override suspend fun read(): String? = stored
        override suspend fun write(nodeId: String) {
            writes.incrementAndGet()
            stored = nodeId
        }
    }

    @Test
    fun `generates and persists an id on first use`() = runTest {
        val store = FakeStore()
        val id = NodeIdentity(store).nodeId()

        assertNotNull(UUID.fromString(id))
        assertEquals(id, store.stored)
        assertEquals(1, store.writes.get())
    }

    @Test
    fun `returns the persisted id instead of generating a new one`() = runTest {
        val existing = "f72a6ef4-2a42-42a9-a39c-9e2dc4f87833"
        val store = FakeStore(stored = existing)

        val id = NodeIdentity(store) { "a-freshly-generated-id" }.nodeId()

        assertEquals(existing, id)
        assertEquals(0, store.writes.get())
    }

    @Test
    fun `is stable across repeated calls`() = runTest {
        val identity = NodeIdentity(FakeStore())
        assertEquals(identity.nodeId(), identity.nodeId())
    }

    @Test
    fun `concurrent first calls agree on one id`() = runTest {
        // The failure this guards against is a node that reports two identities
        // because two callers raced on first launch. A controller sees two
        // nodes, and the duplicate outlives the race that created it.
        val store = FakeStore()
        val generated = AtomicInteger(0)
        val identity = NodeIdentity(store) { "id-${generated.incrementAndGet()}" }

        val ids = (1..32).map { async { identity.nodeId() } }.awaitAll()

        assertEquals(1, ids.distinct().size)
        assertEquals(1, store.writes.get())
        assertTrue("generated more than once", generated.get() == 1)
    }

    @Test
    fun `two fresh nodes never share an id`() = runTest {
        // Guards the privacy rule by construction rather than by review: the
        // default generator takes no input, so there is nothing device-specific
        // it could derive from — two nodes on identical hardware still differ.
        val a = NodeIdentity(FakeStore()).nodeId()
        val b = NodeIdentity(FakeStore()).nodeId()
        assertTrue("two fresh nodes must not share an id", a != b)
    }
}

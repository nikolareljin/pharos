package io.github.nikolareljin.pharos.core.identity

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.UUID

/**
 * Where a node id is persisted. Kept as an interface so the identity rules can
 * be tested without a device: the interesting behaviour is "generated once,
 * then stable forever", and that should not need an Android runtime to prove.
 */
interface NodeIdentityStore {
    suspend fun read(): String?
    suspend fun write(nodeId: String)
}

/**
 * The node's stable identity.
 *
 * Generated on first launch and persisted. Deliberately **not** derived from the
 * MAC address, serial number, advertising id or any account: those identify
 * hardware and people, and a display node needs to identify neither. A random
 * UUID also means a node can be replaced without the replacement inheriting
 * anything the old one was trusted with.
 *
 * Identity is generated exactly once. Two callers racing on first launch must
 * receive the same id — a node that reports two identities looks to a
 * controller like two nodes, and the duplicate outlives the race that caused it.
 */
class NodeIdentity(
    private val store: NodeIdentityStore,
    private val generate: () -> String = { UUID.randomUUID().toString() },
) {
    private val mutex = Mutex()

    @Volatile
    private var cached: String? = null

    suspend fun nodeId(): String {
        cached?.let { return it }
        return mutex.withLock {
            // Re-check inside the lock: another caller may have generated one
            // while this call was waiting for it.
            cached?.let { return@withLock it }
            val existing = store.read()
            val id = existing ?: generate().also { store.write(it) }
            cached = id
            id
        }
    }
}

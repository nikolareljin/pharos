package io.github.nikolareljin.pharos

import android.app.Application
import io.github.nikolareljin.pharos.core.identity.DataStoreNodeIdentityStore
import io.github.nikolareljin.pharos.core.identity.NodeIdentity

/**
 * Application-scoped wiring.
 *
 * Constructed by hand rather than with a dependency-injection framework: at this
 * size the framework would be the largest thing in the build, and one file that
 * shows what depends on what is worth more than the annotations that would
 * replace it. When this stops fitting on a screen, that is the signal to change
 * it.
 */
class PharosApplication : Application() {

    val nodeIdentity: NodeIdentity by lazy {
        NodeIdentity(DataStoreNodeIdentityStore(applicationContext))
    }
}

package io.github.nikolareljin.pharos.core.identity

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first

private val Context.pharosDataStore: DataStore<Preferences> by preferencesDataStore(name = "pharos")

private val NODE_ID = stringPreferencesKey("node_id")

/** DataStore-backed persistence for the node id. */
class DataStoreNodeIdentityStore(private val context: Context) : NodeIdentityStore {

    override suspend fun read(): String? =
        context.pharosDataStore.data.first()[NODE_ID]

    override suspend fun write(nodeId: String) {
        context.pharosDataStore.edit { it[NODE_ID] = nodeId }
    }
}

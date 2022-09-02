package com.tui.tda.hackathon2022.shared

import com.tui.tda.hackathon2022.shared.cache.Database
import com.tui.tda.hackathon2022.shared.cache.DatabaseDriverFactory
import com.tui.tda.hackathon2022.shared.entity.RocketLaunch
import com.tui.tda.hackathon2022.shared.network.SpaceXApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class SpaceXSDK (databaseDriverFactory: DatabaseDriverFactory) {
    private val database = Database(databaseDriverFactory)
    private val api = SpaceXApi()

    @Throws(Exception::class) suspend fun getLaunches(forceReload: Boolean): List<RocketLaunch> {
        val cachedLaunches = database.getAllLaunches()
        return if (cachedLaunches.isNotEmpty() && !forceReload) {
            cachedLaunches
        } else {
            api.getAllLaunches().also {
                database.clearDatabase()
                database.createLaunches(it)
            }
        }
    }

    suspend fun getLaunch(flightNumber: Int): RocketLaunch? = withContext(Dispatchers.Default) {
        database.findLaunch(flightNumber.toLong())
    }
}
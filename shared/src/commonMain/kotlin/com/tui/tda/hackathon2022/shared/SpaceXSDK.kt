package com.tui.tda.hackathon2022.shared

import com.tui.tda.hackathon2022.shared.cache.Database
import com.tui.tda.hackathon2022.shared.cache.DatabaseDriverFactory
import com.tui.tda.hackathon2022.shared.entity.RocketLaunch
import com.tui.tda.hackathon2022.shared.network.SpaceXApi

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

    @Throws(Exception::class) suspend fun getLaunchesByText(queryText: String?): List<RocketLaunch> {
        val cachedLaunches = database.getAllLaunches()
        val finalLanches = if (cachedLaunches.isNotEmpty()) {
            cachedLaunches
        } else {
            api.getAllLaunches().also {
                database.clearDatabase()
                database.createLaunches(it)
            }
        }
        return if (queryText.orEmpty().length > 2) {
            finalLanches.filter {
                it.missionName.contains(queryText.orEmpty(), true)
            }
        } else {
            finalLanches
        }
    }
}
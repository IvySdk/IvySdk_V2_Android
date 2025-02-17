package com.ivy.sdk.base.game.auth

interface IPlayGameAuth : IBaseAuth {

    fun requireServerSideAuthCode(): String?

    fun getPlayGamesUserInfo(): String

    fun unlockAchievement(achievementId: String)

    fun increaseAchievement(achievementId: String, step: Int)

    fun showAchievement()

    fun showLeaderboards()

    fun showLeaderboard(leaderboardId: String)

    fun updateLeaderboard(leaderboardId: String, score: Long)

}
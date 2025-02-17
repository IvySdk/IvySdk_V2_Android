package com.ivy.sdk.base.game.auth


interface IFacebookAuth : IBaseAuth {

    fun getFriends(): String

    fun requireAccessToken(): String?

}
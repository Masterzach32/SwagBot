package xyz.swagbot.api.music

import com.neovisionaries.i18n.CountryCode
import com.wrapper.spotify.SpotifyApi
import com.wrapper.spotify.SpotifyHttpManager
import com.wrapper.spotify.model_objects.specification.Track

object Spotify {

    lateinit var api: SpotifyApi

    private val redirectUri = SpotifyHttpManager.makeUri("https://swagbot.xyz/spotify-connected")

    private val playlistRegex = "^(?:https://open\\.spotify\\.com|spotify)([/:])user\\1([^/]+)\\1playlist\\1([a-zA-Z0-9]+)".toRegex()

    fun login(clientId: String, secret: String) {
        api = SpotifyApi.Builder().setClientId(clientId).setClientSecret(secret).setRedirectUri(redirectUri).build()
        api.accessToken = api.clientCredentials().build().execute().accessToken
    }

    fun getPlaylist(url: String): Playlist? {
        try {
            val result = playlistRegex.find(url)!!
            val user = result.groupValues[2]
            val playlist = result.groupValues[3]

            val info = api.getPlaylist(user, playlist).market(CountryCode.US).build().execute()

            return Playlist(
                    info.name,
                    info.externalUrls["spotify"],
                    info.images.first().url,
                    info.description,
                    info.owner.displayName,
                    info.followers.total,
                    info.tracks.items.map { it.track }
            )
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }

    data class Playlist(
            val name: String,
            val link: String,
            val icon: String,
            val description: String,
            val owner: String,
            val followerCount: Int,
            val tracks: List<Track>
    )
}
// Business logic class
package au.com.shiftyjelly.pocketcasts.repositories.utils

import au.com.shiftyjelly.pocketcasts.models.entity.BaseEpisode
import au.com.shiftyjelly.pocketcasts.repositories.podcast.UserEpisodeManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import org.json.JSONObject
import javax.inject.Inject
import javax.inject.Provider
import kotlinx.coroutines.rx2.await
import okhttp3.Call

class JusSkipItDownloadUtil @Inject constructor(
    private val requestBuilderProvider: Provider<Request.Builder>,
    private val callFactory: Call.Factory
) {

    fun getJusSkipitUrl(
        userEpisodeManager: UserEpisodeManager,
        downloadUrl: String?,
        episode: BaseEpisode,
    ): HttpUrl {

        val payload = mapOf(
            "podcast_name" to episode.title,
            "episode_name" to episode.title,
            "audio_url" to downloadUrl.toString(),
            "remove_ads" to true
        )

        val urlAndToken = runBlocking { userEpisodeManager.getJusskipitPlaybackUrl().await() }
        var playbackUrl: HttpUrl? = urlAndToken.url.toHttpUrlOrNull()
        val token: String = urlAndToken.token

        val jsonPayload = JSONObject(payload).toString()
        val requestBody = jsonPayload.toRequestBody("application/json; charset=utf-8".toMediaType())

        runBlocking {
            if (playbackUrl != null) {
                do {
                    val pollRequest = requestBuilderProvider.get()
                        .url(playbackUrl!!)
                        .header("Authorization", "Bearer $token")
                        .post(requestBody)
                        .build()

                    val pollResponse: Response = callFactory.newCall(pollRequest).execute()
                    pollResponse.use { response ->
                        if (response.code == 202) {
                            delay(30000)
                        } else if (response.isSuccessful) {
                            playbackUrl = response.body?.string()?.toHttpUrlOrNull()
                            return@use
                        }
                    }
                } while (pollResponse.code == 202)
            }
        }

        return playbackUrl ?: throw IllegalStateException("Failed to retrieve JusSkipIt playback URL")
    }
}
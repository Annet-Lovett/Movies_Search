package ru.yandex.practicum.moviessearch.data.network

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import ru.yandex.practicum.moviessearch.data.NetworkClient
import ru.yandex.practicum.moviessearch.data.dto.MovieCastRequest
import ru.yandex.practicum.moviessearch.data.dto.MovieDetailsRequest
import ru.yandex.practicum.moviessearch.data.dto.MoviesSearchRequest
import ru.yandex.practicum.moviessearch.data.dto.NamesSearchRequest
import ru.yandex.practicum.moviessearch.data.dto.Response

class RetrofitNetworkClient(
    private val imdbService: IMDbApiService,
    private val context: Context,
) : NetworkClient {

    override suspend fun doRequest(dto: Any): Response {
        if (isConnected() == false) {
            return Response().apply { resultCode = -1 }
        }
        // Добавили ещё одну проверку
        if ((dto !is MoviesSearchRequest) &&
            (dto !is MovieDetailsRequest) &&
            (dto !is MovieCastRequest) &&
            (dto !is NamesSearchRequest)) {
            return Response().apply { resultCode = 400 }
        }

        return withContext(Dispatchers.IO) {
            try {
            val response =
                when (dto) {
                    is MoviesSearchRequest -> imdbService.searchMovies(dto.expression)
                    is MovieDetailsRequest -> imdbService.getMovieDetails(dto.movieId)
                    is NamesSearchRequest -> imdbService.searchNames(dto.expression)
                    else -> imdbService.getFullCast((dto as MovieCastRequest).movieId)
                }

                response.apply { resultCode = 200 }
                response
            }

            catch (e: Throwable) {
                Response().apply { resultCode = 500 }
            }
        }


    }

    private fun isConnected(): Boolean {
        val connectivityManager = context.getSystemService(
            Context.CONNECTIVITY_SERVICE
        ) as ConnectivityManager
        val capabilities = connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)
        if (capabilities != null) {
            when {
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> return true
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> return true
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> return true
            }
        }
        return false
    }
}

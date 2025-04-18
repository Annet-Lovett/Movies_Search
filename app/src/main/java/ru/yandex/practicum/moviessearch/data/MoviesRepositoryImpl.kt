package ru.yandex.practicum.moviessearch.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import ru.yandex.practicum.moviessearch.data.converters.MovieCastConverter
import ru.yandex.practicum.moviessearch.data.dto.MovieCastRequest
import ru.yandex.practicum.moviessearch.data.dto.MovieCastResponse
import ru.yandex.practicum.moviessearch.data.dto.MovieDetailsRequest
import ru.yandex.practicum.moviessearch.data.dto.MovieDetailsResponse
import ru.yandex.practicum.moviessearch.data.dto.MoviesSearchRequest
import ru.yandex.practicum.moviessearch.data.dto.MoviesSearchResponse
import ru.yandex.practicum.moviessearch.domain.api.MoviesRepository
import ru.yandex.practicum.moviessearch.domain.models.Movie
import ru.yandex.practicum.moviessearch.domain.models.MovieCast
import ru.yandex.practicum.moviessearch.domain.models.MovieDetails
import ru.yandex.practicum.moviessearch.util.Resource

class MoviesRepositoryImpl(
    private val networkClient: NetworkClient,
    private val movieCastConverter: MovieCastConverter,
) : MoviesRepository {

    override fun searchMovies(expression: String): Flow<Resource<List<Movie>>> = flow {
        val response = networkClient.doRequest(MoviesSearchRequest(expression))
        when (response.resultCode) {
            -1 -> {
                emit(Resource.Error("Проверьте подключение к интернету"))
            }
            200 -> {
                with(response as MoviesSearchResponse) {
                    val data = results.map {
                        Movie(it.id, it.resultType, it.image, it.title, it.description)
                    }

                    emit(Resource.Success(data))
                }
            }
            else -> {
                emit(Resource.Error("Ошибка сервера"))
            }
        }
    }

    override fun getMovieDetails(movieId: String): Flow<Resource<MovieDetails>> = flow {
        val response = networkClient.doRequest(MovieDetailsRequest(movieId))
        when (response.resultCode) {
            -1 -> {
                emit(Resource.Error("Проверьте подключение к интернету"))
            }
            200 -> {
                with(response as MovieDetailsResponse) {
                    val data = MovieDetails(
                            id = id,
                            title = title,
                            imDbRating = imDbRating,
                            year = year,
                            countries = countries,
                            genres = genres,
                            directors = directors,
                            writers = writers,
                            stars = stars,
                            plot = plot)

                    emit(Resource.Success(data))
                }

            }
            else -> {
                emit(Resource.Error("Ошибка сервера"))

            }
        }
    }

    override fun getMovieCast(movieId: String): Flow<Resource<MovieCast>> = flow {
        val response = networkClient.doRequest(MovieCastRequest(movieId))
        when (response.resultCode) {
            -1 -> {
                emit(Resource.Error("Проверьте подключение к интернету"))
            }
            200 -> {
                val data = movieCastConverter.convert(response as MovieCastResponse)
                emit(Resource.Success(data))
            }
            else -> {
                emit(Resource.Error("Ошибка сервера"))

            }
        }
    }

}

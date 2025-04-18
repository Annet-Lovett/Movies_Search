package ru.yandex.practicum.moviessearch.presentation.details

import ru.yandex.practicum.moviessearch.domain.models.MovieDetails

sealed interface AboutState {

    data class Content(
        val movieDetails: MovieDetails?
    ) : AboutState

    data class Error(
        val message: String
    ) : AboutState

}
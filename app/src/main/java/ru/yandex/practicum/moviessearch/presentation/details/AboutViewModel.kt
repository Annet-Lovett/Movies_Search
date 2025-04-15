package ru.yandex.practicum.moviessearch.presentation.details

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import ru.yandex.practicum.moviessearch.domain.api.MoviesInteractor

class AboutViewModel(private val movieId: String,
                     private val moviesInteractor: MoviesInteractor, ) : ViewModel() {

    private val stateLiveData = MutableLiveData<AboutState>()
    fun observeState(): LiveData<AboutState> = stateLiveData


    init {

        viewModelScope.launch {
            moviesInteractor
                .getMoviesDetails(movieId)
                .collect { pair ->
                    if (pair.first != null) {
                        stateLiveData.postValue(AboutState.Content(pair.first))
                    } else {
                        stateLiveData.postValue(AboutState.Error(pair.second ?: "Unknown error"))
                    }
                }
        }

    }
}
package ru.yandex.practicum.moviessearch.ui.movies

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import debounce
import org.koin.androidx.viewmodel.ext.android.viewModel
import ru.yandex.practicum.moviessearch.R
import ru.yandex.practicum.moviessearch.databinding.FragmentMoviesBinding
import ru.yandex.practicum.moviessearch.domain.models.Movie
import ru.yandex.practicum.moviessearch.presentation.movies.MoviesState
import ru.yandex.practicum.moviessearch.presentation.movies.MoviesViewModel
import ru.yandex.practicum.moviessearch.ui.details.DetailsFragment
import ru.yandex.practicum.moviessearch.ui.root.RootActivity

class MoviesFragment : Fragment() {

    companion object {
        private const val CLICK_DEBOUNCE_DELAY = 300L
    }

    private val viewModel by viewModel<MoviesViewModel>()

    private var adapter: MoviesAdapter? = null

    private lateinit var onMovieClickDebounce: (Movie) -> Unit

    private var _binding: FragmentMoviesBinding? = null
    private val binding: FragmentMoviesBinding
        get() = _binding!!



    private lateinit var queryInput: EditText
    private lateinit var placeholderMessage: TextView
    private lateinit var moviesList: RecyclerView
    private lateinit var progressBar: ProgressBar
    private lateinit var textWatcher: TextWatcher

//    private var isClickAllowed = true

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentMoviesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        placeholderMessage = binding.placeholderMessage
        queryInput = binding.queryInput
        moviesList = binding.locations
        progressBar = binding.progressBar

        adapter = MoviesAdapter { movie ->
            (activity as RootActivity).animateBottomNavigationView()
            onMovieClickDebounce(movie)
        }

        // Здесь пришлось поправить использование Context
        moviesList.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        moviesList.adapter = adapter

        onMovieClickDebounce = debounce<Movie>(CLICK_DEBOUNCE_DELAY, viewLifecycleOwner.lifecycleScope, false) { movie ->
            findNavController().navigate(R.id.action_moviesFragment_to_detailsFragment,
                DetailsFragment.createArgs(movie.id, movie.image))
        }

        textWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                viewModel.searchDebounce(
                    changedText = s?.toString() ?: ""
                )
            }

            override fun afterTextChanged(s: Editable?) {
            }
        }
        textWatcher.let { queryInput.addTextChangedListener(it) }

        // Здесь пришлось заменить LifecycleOwner на ViewLifecycleOwner
        viewModel.observeState().observe(viewLifecycleOwner) {
            render(it)
        }

        // Здесь пришлось заменить LifecycleOwner на ViewLifecycleOwner
        viewModel.observeShowToast().observe(viewLifecycleOwner) {
            showToast(it)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        textWatcher.let { queryInput.removeTextChangedListener(it) }
        _binding = null
        adapter = null
        moviesList.adapter = null
    }

    private fun showToast(additionalMessage: String?) {
        // Здесь пришлось поправить использование Context
        Toast.makeText(requireContext(), additionalMessage, Toast.LENGTH_LONG).show()
    }

    private fun render(state: MoviesState) {
        when (state) {
            is MoviesState.Content -> showContent(state.movies)
            is MoviesState.Empty -> showEmpty(state.message)
            is MoviesState.Error -> showError(state.message)
            is MoviesState.Loading -> showLoading()
        }
    }

    private fun showLoading() {
        moviesList.visibility = View.GONE
        placeholderMessage.visibility = View.GONE
        progressBar.visibility = View.VISIBLE
    }

    private fun showError(errorMessage: String) {
        moviesList.visibility = View.GONE
        placeholderMessage.visibility = View.VISIBLE
        progressBar.visibility = View.GONE

        placeholderMessage.text = errorMessage
    }

    private fun showEmpty(emptyMessage: String) {
        showError(emptyMessage)
    }

    private fun showContent(movies: List<Movie>) {
        moviesList.visibility = View.VISIBLE
        placeholderMessage.visibility = View.GONE
        progressBar.visibility = View.GONE

        adapter?.movies?.clear()
        adapter?.movies?.addAll(movies)
        adapter?.notifyDataSetChanged()
    }

//    private fun clickDebounce(): Boolean {
//        val current = isClickAllowed
//        if (isClickAllowed) {
//            isClickAllowed = false
//            viewLifecycleOwner.lifecycleScope.launch {
//                delay(CLICK_DEBOUNCE_DELAY)
//                isClickAllowed = true
//            }
//        }
//        return current
//    }

}
package com.example.pokedex.pokemonlist

import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.palette.graphics.Palette
import com.example.pokedex.models.PokemonEntry
import com.example.pokedex.repository.PokemonRepository
import com.example.pokedex.util.Constants.PAGE_SIZE
import com.example.pokedex.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class PokemonListViewModel @Inject constructor(
    private val repository: PokemonRepository
) : ViewModel() {

    // Current page = 0
    private var curPage = 0

    // Add a mutableStatOf for searchQuery
    var searchQuery = mutableStateOf("")

    // A list to store the Pokemon Data
    var pokemonList = mutableStateOf<List<PokemonEntry>>(listOf())

    //  A string to store any error messages during data loading
    var loadError = mutableStateOf("")

    // A boolean to indicate whether data is currently being loading
    var isLoading = mutableStateOf(false)

    // A boolean to indicated if all data has been loaded
    var endReached = mutableStateOf(false)

    // A cached pokemon list
    private var cachedPokemonList = listOf<PokemonEntry>()

    // To save initial pokemon list in this cached pokemon list when we actually started the search
    private var isSearchStarting = true

    // This is actually true as search results are actually displaying as long as the search field actually contains something
    var isSearching = mutableStateOf(false)

    // Constructor
    init {
        // When the ViewModel is created, initiate the data loading
        loadPokemonPaginated()
    }

    private fun trimQuery(query: String): String {
        return query.trim()
    }

    fun searchPokemonList(query: String) {

        // Update searchQuery when a new query is entered
        searchQuery.value = query

        val listToSearch = if(isSearchStarting) {
            pokemonList.value
        } else {
            cachedPokemonList
        }
        viewModelScope.launch(Dispatchers.Default) {

            val trimmedQuery = trimQuery(query)
            Log.d("SearchQuery", "Query: $trimmedQuery")

            if(trimmedQuery.isEmpty()) {
                pokemonList.value = cachedPokemonList
                isSearching.value = false
                isSearchStarting = true
                return@launch
            }
            val results = listToSearch.filter {
                it.pokemonName.contains(trimmedQuery, ignoreCase = true) ||
                        it.number.toString() == trimmedQuery
            }
            if(isSearchStarting) {
                cachedPokemonList = pokemonList.value
                isSearchStarting = false
            }
            pokemonList.value = results
        }
    }

    // Function to load Pokemon data in a paginated manner
    fun loadPokemonPaginated() {
        viewModelScope.launch {
            // Set isLoading to true to indicate data loading is in progress
            isLoading.value = true


            // Fetch data from the repository
            // Current Page * 20 PAGE SIZE
            when (val result = repository.getPokemonList(PAGE_SIZE, curPage * PAGE_SIZE)) {
                is Resource.Success -> {
                    // Update endReached to indicate if all data has been loaded
                    endReached.value = curPage * PAGE_SIZE >= result.data!!.count

                    // Process and map data into PokedexListEntry objects
                    val pokedexEntries = result.data.results.mapIndexed { index, entry ->
                        // Extract the Pokemon number from the URL
                        val number = if (entry.url.endsWith("/")) {
                            entry.url.dropLast(1).takeLastWhile { it.isDigit() }
                        } else {
                            entry.url.takeLastWhile { it.isDigit() }
                        }
                        val pokemonIndex = number.toInt()
                        val url =
                            "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/${pokemonIndex}.png"

                        // Debug: Log the URL
                        Log.d("ImageLoading", "Loading image for Pokemon $pokemonIndex from URL: $url")

                        PokemonEntry(entry.name.replaceFirstChar {
                            if (it.isLowerCase()) it.titlecase(
                                Locale.ROOT
                            ) else it.toString()
                        }, url,number.toInt())
                    }
                    // Increment the current page
                    curPage++

                    // Clear any previous error message
                    loadError.value = ""

                    // Not loading anymore
                    // Set isLoading to false as data loading is complete
                    isLoading.value = false

                    // Append the new data to the existing list
                    pokemonList.value += pokedexEntries
                }
                is Resource.Error -> {
                    // Update loadError with the error message
                    loadError.value = result.message!!

                    // Set isLoading to false to indicate data loading is complete
                    isLoading.value = false

                }
            }
        }
    }

    // Function to calculate the dominate color of a Drawable
    fun calcDominantColor(drawable: Drawable, onFinish: (Color) -> Unit) {
        val bmp = (drawable as BitmapDrawable).bitmap.copy(Bitmap.Config.ARGB_8888, true)

        Palette.from(bmp).generate { palette ->
            palette?.dominantSwatch?.rgb?.let { colorValue ->
                onFinish(Color(colorValue))
            }
        }
    }

}
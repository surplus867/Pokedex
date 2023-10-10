package com.example.pokedex.util

// Define a sealed class Resource with a generic type T to represent different states of data retrieval or operations
sealed class Resource<T>(val data: T? = null, val message: String? = null) {

    // Subclass representing the success state, holding successfully retrieved data.
    class Success<T>(data: T) : Resource<T>(data)

    // Subclass representing the error state, holding an error message and optional associated data.
    class Error<T>(message: String, data: T? = null) : Resource<T>(data, message)

    // Subclass representing the loading state, with an optional associated data.
    class Loading<T>(data: T? = null) : Resource<T>(data)
}

package com.example.pokedex

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.pokedex.pokemonlist.PokemonListScreen
import com.example.pokedex.ui.theme.PokedexTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            PokedexTheme {
                val navController = rememberNavController()
                NavHost(
                    navController = navController,
                    startDestination = "pokemon_list_screen"
                ) {
                    composable("pokemon_list_screen") {
                        // Placeholder content for tge villager list screen
                        //Text(text = "pokemon List Screen")
                        PokemonListScreen(navController = navController)

                    }
                    composable(
                        "pokemon_detail_screen/{dominantColor}/{pokemonName}",
                        arguments = listOf(
                            navArgument("dominantColor") {
                                type = NavType.IntType
                            },
                            navArgument("pokemonName") {
                                type = NavType.StringType
                            }
                        )
                    ) { backStackEntry ->
                        // Retrieve the arguments from the backStackEntry
                        val dominantColor = backStackEntry.arguments?.getInt("dominantColor") ?.let {
                            Color(it)
                        } ?: Color.White
                        val pokemonName = backStackEntry.arguments?.getString("pokemonName") ?: ""

                        // Placeholder content for the villager detail screen
                        Surface(
                            modifier = Modifier.fillMaxSize(),
                            color = dominantColor
                        ) {
                            Text(
                                text = "pokemon Name: $pokemonName",
                                color = Color.Black
                            )
                        }
                    }
                }

            }
        }
    }
}
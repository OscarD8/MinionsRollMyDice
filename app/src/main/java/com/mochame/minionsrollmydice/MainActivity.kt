package com.mochame.minionsrollmydice

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import com.mochame.minionsrollmydice.di.AppModule
import com.mochame.minionsrollmydice.ui.CharacterSheetViewModel
import com.mochame.minionsrollmydice.ui.DiceRollerViewModel
import com.mochame.minionsrollmydice.ui.MainAppScreen
import com.mochame.minionsrollmydice.ui.theme.MinionsRollMyDiceTheme
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.annotation.KoinApplication
import org.koin.plugin.module.dsl.startKoin

// Define the Koin Application container with Modules.
// This executes independently without KSP for FIR reflection, avoiding generated class lookups.
@KoinApplication(modules = [AppModule::class])
object MinionsApp

class MainActivity : ComponentActivity() {

    // ViewModels are injected asynchronously using Koin dependency injection delegates
    private val characterSheetViewModel: CharacterSheetViewModel by viewModel()
    private val diceRollerViewModel: DiceRollerViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Initialize Koin DI framework with compiler plugin resolution
        startKoin<MinionsApp> {
            androidContext(this@MainActivity)
        }

        setContent {
            MinionsRollMyDiceTheme {
                MainAppScreen(
                    characterSheetViewModel = characterSheetViewModel,
                    diceRollerViewModel = diceRollerViewModel,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}
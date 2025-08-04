package com.example.thinksmarter

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.thinksmarter.data.db.AppDatabase
import com.example.thinksmarter.data.repository.ThinkSmarterRepositoryImpl
import com.example.thinksmarter.ui.screens.MainScreen
import com.example.thinksmarter.ui.screens.RecentQuestionsScreen
import com.example.thinksmarter.ui.screens.SettingsScreen
import com.example.thinksmarter.ui.screens.StatisticsScreen
import com.example.thinksmarter.ui.screens.DailyChallengeScreen
import com.example.thinksmarter.ui.theme.ThinkSmarterTheme
import com.example.thinksmarter.ui.viewmodel.MainViewModel
import com.example.thinksmarter.ui.viewmodel.SettingsViewModel
import com.example.thinksmarter.ui.viewmodel.StatisticsViewModel
import com.example.thinksmarter.ui.viewmodel.DailyChallengeViewModel
import com.example.thinksmarter.ui.viewmodel.RecentQuestionsViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val database = AppDatabase.getDatabase(this)
        val repository = ThinkSmarterRepositoryImpl(this, database)
        
        setContent {
            ThinkSmarterTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    ThinkSmarterApp(repository = repository)
                }
            }
        }
        
        // Initialize default categories after UI is set up
        CoroutineScope(Dispatchers.IO).launch {
            try {
                repository.initializeDefaultCategories()
            } catch (e: Exception) {
                // Handle initialization error silently
            }
        }
    }
}

@Composable
fun ThinkSmarterApp(repository: ThinkSmarterRepositoryImpl) {
    val navController = rememberNavController()
    
    NavHost(navController = navController, startDestination = "main") {
        composable("main") {
            val mainViewModel: MainViewModel = viewModel {
                MainViewModel(repository)
            }
            val uiState by mainViewModel.uiState.collectAsState()
            
            MainScreen(
                uiState = uiState,
                onEvent = mainViewModel::handleEvent,
                onNavigateToSettings = { navController.navigate("settings") },
                onNavigateToStatistics = { navController.navigate("statistics") },
                onNavigateToDailyChallenge = { navController.navigate("daily_challenge") },
                onNavigateToRecentQuestions = { navController.navigate("recent_questions") }
            )
        }
        
        composable("settings") {
            val settingsViewModel: SettingsViewModel = viewModel {
                SettingsViewModel(repository)
            }
            val uiState by settingsViewModel.uiState.collectAsState()
            
            SettingsScreen(
                uiState = uiState,
                onEvent = settingsViewModel::handleEvent,
                onNavigateBack = { navController.popBackStack() }
            )
        }
        
        composable("statistics") {
            val statisticsViewModel: StatisticsViewModel = viewModel {
                StatisticsViewModel(repository)
            }
            val uiState by statisticsViewModel.uiState.collectAsState()
            
            StatisticsScreen(
                uiState = uiState,
                onNavigateBack = { navController.popBackStack() }
            )
        }
        
        composable("daily_challenge") {
            val dailyChallengeViewModel: DailyChallengeViewModel = viewModel {
                DailyChallengeViewModel(repository)
            }
            val uiState by dailyChallengeViewModel.uiState.collectAsState()
            
            DailyChallengeScreen(
                uiState = uiState,
                onEvent = dailyChallengeViewModel::handleEvent,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable("recent_questions") {
            val recentQuestionsViewModel: RecentQuestionsViewModel = viewModel {
                RecentQuestionsViewModel(repository)
            }
            val uiState by recentQuestionsViewModel.uiState.collectAsState()

            RecentQuestionsScreen(
                questionsWithAnswers = uiState.questionsWithAnswers,
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
} 
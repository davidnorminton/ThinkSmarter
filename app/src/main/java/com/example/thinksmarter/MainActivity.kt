package com.example.thinksmarter

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.thinksmarter.data.auth.AuthState
import com.example.thinksmarter.data.auth.UserAuthManager
import com.example.thinksmarter.data.model.QuestionWithAnswer
import com.example.thinksmarter.ui.screens.CategoryManagementScreen
import com.example.thinksmarter.ui.screens.DailyChallengeScreen
import com.example.thinksmarter.ui.screens.MainScreen
import com.example.thinksmarter.ui.screens.QuestionDetailScreen
import com.example.thinksmarter.ui.screens.RandomFactsScreen
import com.example.thinksmarter.ui.screens.RecentQuestionsScreen
import com.example.thinksmarter.ui.screens.SettingsScreen
import com.example.thinksmarter.ui.screens.StatisticsScreen
import com.example.thinksmarter.ui.screens.TextImprovementScreen
import com.example.thinksmarter.ui.screens.UserProfileScreen
import com.example.thinksmarter.ui.theme.ThinkSmarterTheme
import com.example.thinksmarter.ui.viewmodel.CategoryViewModel
import com.example.thinksmarter.ui.viewmodel.DailyChallengeViewModel
import com.example.thinksmarter.ui.viewmodel.MainViewModel
import com.example.thinksmarter.ui.viewmodel.MainUiEvent
import com.example.thinksmarter.ui.viewmodel.RandomFactsViewModel
import com.example.thinksmarter.ui.viewmodel.RecentQuestionsViewModel
import com.example.thinksmarter.ui.viewmodel.SettingsViewModel
import com.example.thinksmarter.ui.viewmodel.StatisticsViewModel
import com.example.thinksmarter.ui.viewmodel.TextImprovementViewModel
import com.example.thinksmarter.ui.viewmodel.UserProfileViewModel
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var userAuthManager: UserAuthManager
    private lateinit var googleSignInLauncher: androidx.activity.result.ActivityResultLauncher<android.content.Intent>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize Google Sign-In launcher
        googleSignInLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            val data = result.data
            // Handle the sign-in result
            println("DEBUG: Google Sign-In result received")
            userAuthManager.handleSignInResult(data)
        }

        try {
            println("DEBUG: Starting MainActivity onCreate")

            setContent {
                ThinkSmarterTheme {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.background
                    ) {
                        val navController = rememberNavController()
                        
                        NavHost(navController = navController, startDestination = "main") {
                            composable("main") {
                                val mainViewModel: MainViewModel = hiltViewModel()
                                val uiState by mainViewModel.uiState.collectAsState()

                                // Observe UserAuthManager state and update MainViewModel
                                LaunchedEffect(Unit) {
                                    userAuthManager.authState.collect { authState ->
                                        when (authState) {
                                            is AuthState.Authenticated -> {
                                                mainViewModel.handleEvent(MainUiEvent.UpdateUserProfile(authState.user))
                                            }
                                            is AuthState.NotAuthenticated -> {
                                                mainViewModel.handleEvent(MainUiEvent.UpdateUserProfile(null))
                                            }
                                            else -> {
                                                // Loading or Error states - keep current profile
                                            }
                                        }
                                    }
                                }

                                MainScreen(
                                    uiState = uiState,
                                    onEvent = mainViewModel::handleEvent,
                                    onNavigateToSettings = { navController.navigate("settings") },
                                    onNavigateToStatistics = { navController.navigate("statistics") },
                                    onNavigateToDailyChallenge = { navController.navigate("daily_challenge") },
                                    onNavigateToRecentQuestions = { navController.navigate("recent_questions") },
                                    onNavigateToTextImprovement = { navController.navigate("text_improvement") },
                                    onNavigateToRandomFacts = { navController.navigate("random_facts") },
                                    onNavigateToUserProfile = { navController.navigate("user_profile") }
                                )
                            }
                            
                            composable("settings") {
                                val settingsViewModel: SettingsViewModel = hiltViewModel()
                                val uiState by settingsViewModel.uiState.collectAsState()

                                SettingsScreen(
                                    uiState = uiState,
                                    onEvent = settingsViewModel::handleEvent,
                                    onNavigateBack = { navController.popBackStack() },
                                    onNavigateToManageCategories = { navController.navigate("category_management") }
                                )
                            }
                            
                            composable("category_management") {
                                val categoryViewModel: CategoryViewModel = hiltViewModel()
                                val categories by categoryViewModel.categories.collectAsState()

                                CategoryManagementScreen(
                                    categories = categories,
                                    onAddCategory = categoryViewModel::addCategory,
                                    onDeleteCategory = categoryViewModel::deleteCategory,
                                    onNavigateBack = { navController.popBackStack() }
                                )
                            }
                            
                            composable("statistics") {
                                val statisticsViewModel: StatisticsViewModel = hiltViewModel()
                                val uiState by statisticsViewModel.uiState.collectAsState()

                                StatisticsScreen(
                                    uiState = uiState,
                                    onNavigateBack = { navController.popBackStack() }
                                )
                            }
                            
                            composable("daily_challenge") {
                                val dailyChallengeViewModel: DailyChallengeViewModel = hiltViewModel()
                                val uiState by dailyChallengeViewModel.uiState.collectAsState()

                                DailyChallengeScreen(
                                    uiState = uiState,
                                    onEvent = dailyChallengeViewModel::handleEvent,
                                    onNavigateBack = { navController.popBackStack() }
                                )
                            }
                            
                            composable("recent_questions") {
                                val recentQuestionsViewModel: RecentQuestionsViewModel = hiltViewModel()
                                val uiState by recentQuestionsViewModel.uiState.collectAsState()

                                RecentQuestionsScreen(
                                    questionsWithAnswers = uiState.questionsWithAnswers,
                                    onNavigateBack = { navController.popBackStack() },
                                    onQuestionClick = { questionWithAnswer ->
                                        // Navigate to question detail with the question data
                                        navController.currentBackStackEntry?.savedStateHandle?.set("questionWithAnswer", questionWithAnswer)
                                        navController.navigate("question_detail")
                                    }
                                )
                            }
                            
                            composable("question_detail") {
                                val questionWithAnswer = navController.previousBackStackEntry?.savedStateHandle?.get<QuestionWithAnswer>("questionWithAnswer")
                                
                                questionWithAnswer?.let { question ->
                                    QuestionDetailScreen(
                                        questionWithAnswer = question,
                                        onNavigateBack = { navController.popBackStack() }
                                    )
                                }
                            }
                            
                            composable("text_improvement") {
                                val textImprovementViewModel: TextImprovementViewModel = hiltViewModel()
                                val uiState by textImprovementViewModel.uiState.collectAsState()

                                TextImprovementScreen(
                                    uiState = uiState,
                                    onEvent = textImprovementViewModel::onEvent,
                                    onNavigateBack = { navController.popBackStack() }
                                )
                            }
                            
                            composable("random_facts") {
                                val randomFactsViewModel: RandomFactsViewModel = hiltViewModel()
                                val uiState by randomFactsViewModel.uiState.collectAsState()

                                RandomFactsScreen(
                                    uiState = uiState,
                                    onEvent = randomFactsViewModel::handleEvent,
                                    onNavigateBack = { navController.popBackStack() }
                                )
                            }
                            
                            composable("user_profile") {
                                val userProfileViewModel: UserProfileViewModel = hiltViewModel()
                                val uiState by userProfileViewModel.uiState.collectAsState()
                                
                                // Handle Google Sign-In
                                if (uiState.authState is AuthState.Loading) {
                                    userProfileViewModel.signInWithGoogle(this@MainActivity.googleSignInLauncher)
                                }
                                
                                UserProfileScreen(
                                    uiState = uiState,
                                    onEvent = userProfileViewModel::handleEvent,
                                    onNavigateBack = { navController.popBackStack() }
                                )
                            }
                        }
                    }
                }
            }
        } catch (e: Exception) {
            println("DEBUG: Fatal error in MainActivity: ${e.message}")
            e.printStackTrace()
            throw e
        }
    }
}

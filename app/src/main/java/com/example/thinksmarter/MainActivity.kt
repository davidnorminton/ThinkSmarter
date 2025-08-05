package com.example.thinksmarter

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.example.thinksmarter.data.db.AppDatabase
import com.example.thinksmarter.data.repository.ThinkSmarterRepositoryImpl
import com.example.thinksmarter.ui.screens.MainScreen
import com.example.thinksmarter.ui.screens.RecentQuestionsScreen
import com.example.thinksmarter.ui.screens.SettingsScreen
import com.example.thinksmarter.ui.screens.StatisticsScreen
import com.example.thinksmarter.ui.screens.DailyChallengeScreen
import com.example.thinksmarter.ui.screens.TextImprovementScreen
import com.example.thinksmarter.ui.screens.QuestionDetailScreen
import com.example.thinksmarter.ui.screens.UserProfileScreen
import com.example.thinksmarter.data.model.QuestionWithAnswer
import com.example.thinksmarter.ui.theme.ThinkSmarterTheme
import com.example.thinksmarter.ui.viewmodel.MainViewModel
import com.example.thinksmarter.ui.viewmodel.SettingsViewModel
import com.example.thinksmarter.ui.viewmodel.StatisticsViewModel
import com.example.thinksmarter.ui.viewmodel.DailyChallengeViewModel
import com.example.thinksmarter.ui.viewmodel.RecentQuestionsViewModel
import com.example.thinksmarter.ui.viewmodel.TextImprovementViewModel
import com.example.thinksmarter.ui.viewmodel.UserProfileViewModel
import com.example.thinksmarter.data.auth.UserAuthManager
import com.example.thinksmarter.data.auth.AuthState
import androidx.activity.result.contract.ActivityResultContracts
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import androidx.compose.runtime.LaunchedEffect
import com.example.thinksmarter.ui.viewmodel.MainUiEvent

class MainActivity : ComponentActivity() {
    
    private lateinit var userAuthManager: UserAuthManager
    private lateinit var googleSignInLauncher: androidx.activity.result.ActivityResultLauncher<android.content.Intent>
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Initialize UserAuthManager
        userAuthManager = UserAuthManager(this)
        
        // Initialize Google Sign-In launcher
        googleSignInLauncher = registerForActivityResult(
            androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult()
        ) { result ->
            val data = result.data
            // Handle the sign-in result
            println("DEBUG: Google Sign-In result received")
            userAuthManager.handleSignInResult(data)
        }
        
        try {
            println("DEBUG: Starting MainActivity onCreate")
            
            // Initialize database
            println("DEBUG: Starting database initialization")
            val database = try {
                AppDatabase.getDatabase(this).also {
                    println("DEBUG: Testing database access")
                    it.questionDao() // Test database access
                    println("DEBUG: Database access successful")
                }
            } catch (e: Exception) {
                println("DEBUG: Error initializing database: ${e.message}")
                e.printStackTrace()
                throw e
            }
            println("DEBUG: Database initialized successfully")
            
            // Create repository
            println("DEBUG: Creating repository")
            val repository = try {
                ThinkSmarterRepositoryImpl(this, database)
            } catch (e: Exception) {
                println("DEBUG: Error creating repository: ${e.message}")
                e.printStackTrace()
                throw e
            }
            println("DEBUG: Repository created successfully")
            
            // Initialize default categories
            println("DEBUG: Initializing default categories")
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    repository.initializeDefaultCategories()
                    println("DEBUG: Default categories initialized successfully")
                } catch (e: Exception) {
                    println("DEBUG: Error initializing default categories: ${e.message}")
                    e.printStackTrace()
                }
            }
            
            try {
                println("DEBUG: Starting setContent")
                setContent {
                    println("DEBUG: Inside setContent block")
                    ThinkSmarterTheme {
                        Surface(
                            modifier = Modifier.fillMaxSize(),
                            color = MaterialTheme.colorScheme.background
                        ) {
                            println("DEBUG: Setting up navigation")
                            val navController = rememberNavController()
                            
                            NavHost(navController = navController, startDestination = "main") {
                                composable("main") {
                                    println("DEBUG: Creating MainViewModel")
                                    val mainViewModel: MainViewModel = viewModel {
                                        MainViewModel(repository)
                                    }
                                    val uiState by mainViewModel.uiState.collectAsState()
                                    println("DEBUG: Main screen uiState: $uiState")
                                    
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
                                        onNavigateToUserProfile = { navController.navigate("user_profile") }
                                    )
                                }
                                
                                composable("settings") {
                                    println("DEBUG: Creating SettingsViewModel")
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
                                    println("DEBUG: Creating StatisticsViewModel")
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
                                    println("DEBUG: Creating DailyChallengeViewModel")
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
                                    println("DEBUG: Creating RecentQuestionsViewModel")
                                    val recentQuestionsViewModel: RecentQuestionsViewModel = viewModel {
                                        RecentQuestionsViewModel(repository)
                                    }
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
                                    println("DEBUG: Creating TextImprovementViewModel")
                                    val textImprovementViewModel: TextImprovementViewModel = viewModel {
                                        TextImprovementViewModel(repository)
                                    }
                                    val uiState by textImprovementViewModel.uiState.collectAsState()
                                    println("DEBUG: TextImprovement uiState: $uiState")

                                    TextImprovementScreen(
                                        uiState = uiState,
                                        onEvent = textImprovementViewModel::onEvent,
                                        onNavigateBack = { navController.popBackStack() }
                                    )
                                }
                                
                                composable("user_profile") {
                                    val userProfileViewModel: UserProfileViewModel = viewModel {
                                        UserProfileViewModel(userAuthManager)
                                    }
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
                println("DEBUG: setContent completed")
            } catch (e: Exception) {
                println("DEBUG: Error in setContent: ${e.message}")
                e.printStackTrace()
                throw e
            }
        } catch (e: Exception) {
            println("DEBUG: Fatal error in MainActivity: ${e.message}")
            e.printStackTrace()
            throw e
        }
    }
}
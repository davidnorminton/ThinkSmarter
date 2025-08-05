package com.example.thinksmarter.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import kotlinx.coroutines.launch
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.Color
import com.example.thinksmarter.data.model.Category
import com.example.thinksmarter.data.model.QuestionWithAnswer
import com.example.thinksmarter.ui.components.*
import com.example.thinksmarter.ui.theme.*
import com.example.thinksmarter.ui.viewmodel.MainUiEvent
import com.example.thinksmarter.ui.viewmodel.MainUiState
import com.example.thinksmarter.ui.viewmodel.MainViewModel
import com.example.thinksmarter.domain.repository.AnswerEvaluation

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    uiState: MainUiState,
    onEvent: (MainUiEvent) -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToStatistics: () -> Unit,
    onNavigateToDailyChallenge: () -> Unit,
    onNavigateToRecentQuestions: () -> Unit,
    onNavigateToTextImprovement: () -> Unit,
    onNavigateToRandomFacts: () -> Unit,
    onNavigateToUserProfile: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (uiState.isLoading) {
        SplashScreen(modifier = modifier)
    } else {
        val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
        val scope = rememberCoroutineScope()

        ModalNavigationDrawer(
            drawerContent = {
                DrawerMenu(
                    onNavigateToSettings = onNavigateToSettings,
                    onNavigateToStatistics = onNavigateToStatistics,
                    onNavigateToDailyChallenge = onNavigateToDailyChallenge,
                    onNavigateToRecentQuestions = onNavigateToRecentQuestions,
                    onNavigateToTextImprovement = onNavigateToTextImprovement,
                    onNavigateToRandomFacts = onNavigateToRandomFacts,
                    onNavigateToUserProfile = onNavigateToUserProfile,
                    userProfile = uiState.userProfile,
                    onClose = { 
                        scope.launch {
                            drawerState.close()
                        }
                    }
                )
            },
            drawerState = drawerState,
            gesturesEnabled = drawerState.isOpen
        ) {
            Scaffold(
                topBar = {
                    TopAppBar(
                        title = { Text("ThinkSmarter") },
                        navigationIcon = {
                            IconButton(
                                onClick = { 
                                    scope.launch {
                                        drawerState.open()
                                    }
                                }
                            ) {
                                Icon(Icons.Default.Menu, contentDescription = "Menu")
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = MaterialTheme.colorScheme.surface,
                            titleContentColor = MaterialTheme.colorScheme.onSurface,
                            navigationIconContentColor = MaterialTheme.colorScheme.onSurface
                        )
                    )
                },
                bottomBar = {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        // Show submit button when question exists and not evaluating
                        if (uiState.currentQuestion != null && !uiState.isEvaluatingAnswer) {
                            FloatingActionButton(
                                onClick = { onEvent(MainUiEvent.SubmitAnswer) },
                                modifier = Modifier.fillMaxWidth(),
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary,
                                elevation = FloatingActionButtonDefaults.elevation(
                                    defaultElevation = 6.dp,
                                    pressedElevation = 12.dp
                                )
                            ) {
                                Row(
                                    modifier = Modifier
                                        .padding(horizontal = 16.dp, vertical = 8.dp),
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    if (uiState.isEvaluatingAnswer) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(20.dp),
                                            color = MaterialTheme.colorScheme.onPrimary,
                                            strokeWidth = 2.dp
                                        )
                                    } else {
                                        Icon(
                                            Icons.Default.Send,
                                            contentDescription = "Submit Answer",
                                            modifier = Modifier.size(24.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "Submit Answer",
                                            style = MaterialTheme.typography.labelLarge
                                        )
                                    }
                                }
                            }
                        } else {
                            // Show generate question button
                            FloatingActionButton(
                                onClick = { onEvent(MainUiEvent.GenerateQuestion) },
                                modifier = Modifier.fillMaxWidth(),
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary,
                                elevation = FloatingActionButtonDefaults.elevation(
                                    defaultElevation = 6.dp,
                                    pressedElevation = 12.dp
                                )
                            ) {
                                Row(
                                    modifier = Modifier
                                        .padding(horizontal = 16.dp, vertical = 8.dp),
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    if (uiState.isGeneratingQuestion) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(20.dp),
                                            color = MaterialTheme.colorScheme.onPrimary,
                                            strokeWidth = 2.dp
                                        )
                                    } else {
                                        Icon(
                                            Icons.Default.Add,
                                            contentDescription = "Ask Question",
                                            modifier = Modifier.size(24.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "Ask Question",
                                            style = MaterialTheme.typography.labelLarge
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            ) { paddingValues ->
                LazyColumn(
                    modifier = modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(top = 16.dp, bottom = 96.dp) // Extra padding for bottom button
                ) {
                    // Error message
                    uiState.error?.let { error ->
                        item {
                            ModernCard(
                                modifier = Modifier.fillMaxWidth(),
                                content = {
                                    Row(
                                        modifier = Modifier.padding(16.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            Icons.Default.Warning,
                                            contentDescription = "Error",
                                            tint = MaterialTheme.colorScheme.error,
                                            modifier = Modifier.size(24.dp)
                                        )
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Text(
                                            text = error,
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.error
                                        )
                                    }
                                }
                            )
                        }
                    }

                    // Current question - hide when generating, evaluating, or when evaluation is present
                    item {
                        uiState.currentQuestion?.let { question ->
                            if (!uiState.isGeneratingQuestion && !uiState.isEvaluatingAnswer && uiState.evaluation == null) {
                                ModernCard(
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(
                                        modifier = Modifier.padding(20.dp)
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                Icons.Default.Create,
                                                contentDescription = "Question",
                                                tint = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.size(24.dp)
                                            )
                                            Spacer(modifier = Modifier.width(12.dp))
                                            Text(
                                                text = "Current Question",
                                                style = MaterialTheme.typography.titleMedium,
                                                fontWeight = FontWeight.Medium
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(16.dp))
                                        Text(
                                            text = question.text,
                                            style = MaterialTheme.typography.bodyLarge,
                                            lineHeight = MaterialTheme.typography.bodyLarge.lineHeight * 1.2f
                                        )
                                        Spacer(modifier = Modifier.height(16.dp))
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            ModernChip(
                                                text = "Level ${question.difficulty}",
                                                onClick = { },
                                                selected = true,
                                                modifier = Modifier.weight(1f)
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            ModernChip(
                                                text = question.category,
                                                onClick = { },
                                                selected = true,
                                                modifier = Modifier.weight(1f)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Expected length card - hide when generating, evaluating, or when evaluation is present
                    item {
                        uiState.currentQuestion?.let { question ->
                            if (!uiState.isGeneratingQuestion && !uiState.isEvaluatingAnswer && uiState.evaluation == null) {
                                ModernCard(
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier.padding(16.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            Icons.Default.Create,
                                            contentDescription = "Answer Length",
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(24.dp)
                                        )
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Column {
                                            Text(
                                                text = "Expected Answer Length",
                                                style = MaterialTheme.typography.titleSmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                            Text(
                                                text = when {
                                                    question.difficulty <= 3 -> "Short (1-3 sentences)"
                                                    question.difficulty <= 7 -> "Medium (4-6 sentences)"
                                                    else -> "Long (7+ sentences)"
                                                },
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Answer input - only show when question is available, not evaluating, and no evaluation present
                    item {
                        uiState.currentQuestion?.let { question ->
                            if (!uiState.isEvaluatingAnswer && !uiState.isGeneratingQuestion && uiState.evaluation == null) {
                                val focusRequester = remember { FocusRequester() }
                                
                                LaunchedEffect(Unit) {
                                    focusRequester.requestFocus()
                                }
                                
                                ModernTextField(
                                    value = uiState.userAnswer,
                                    onValueChange = { onEvent(MainUiEvent.UpdateUserAnswer(it)) },
                                    label = "Your Answer",
                                    placeholder = "Type your thoughtful response here...",
                                    minLines = 4,
                                    maxLines = 8,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .focusRequester(focusRequester)
                                )
                            }
                        }
                    }

                    // Difficulty slider - hide when generating, evaluating, or when evaluation is present
                    item {
                        if (!uiState.isGeneratingQuestion && !uiState.isEvaluatingAnswer && uiState.evaluation == null) {
                            ModernCard(
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(
                                    modifier = Modifier.padding(20.dp)
                                ) {
                                    Text(
                                        text = "Difficulty Level",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Medium
                                    )
                                    Spacer(modifier = Modifier.height(16.dp))
                                    DifficultySlider(
                                        value = uiState.difficultyLevel,
                                        onValueChange = { onEvent(MainUiEvent.SetDifficultyLevel(it)) }
                                    )
                                }
                            }
                        }
                    }

                    // Category selection - hide when generating, evaluating, or when evaluation is present
                    item {
                        if (!uiState.isGeneratingQuestion && !uiState.isEvaluatingAnswer && uiState.evaluation == null) {
                            ModernCard(
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(
                                    modifier = Modifier.padding(20.dp)
                                ) {
                                    Text(
                                        text = "Question Category",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Medium
                                    )
                                    Spacer(modifier = Modifier.height(16.dp))
                                    CategorySelector(
                                        selectedCategory = uiState.selectedCategory,
                                        availableCategories = uiState.availableCategories,
                                        onCategorySelected = { onEvent(MainUiEvent.SetSelectedCategory(it)) },
                                        onAddCategory = { /* TODO: Add category dialog */ }
                                    )
                                }
                            }
                        }
                    }

                    // Loading state for question generation
                    item {
                        if (uiState.isGeneratingQuestion) {
                            Box(
                                modifier = Modifier.fillMaxWidth(),
                                contentAlignment = Alignment.Center
                            ) {
                                ModernCard(
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(
                                        modifier = Modifier.padding(20.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        CircularProgressIndicator(
                                            color = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(48.dp)
                                        )
                                        Spacer(modifier = Modifier.height(16.dp))
                                        Text(
                                            text = "Generating your question...",
                                            style = MaterialTheme.typography.bodyLarge,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text(
                                            text = "This may take a few seconds",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Loading state for answer evaluation
                    item {
                        if (uiState.isEvaluatingAnswer) {
                            Box(
                                modifier = Modifier.fillMaxWidth(),
                                contentAlignment = Alignment.Center
                            ) {
                                ModernCard(
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(
                                        modifier = Modifier.padding(20.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        CircularProgressIndicator(
                                            color = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(48.dp)
                                        )
                                        Spacer(modifier = Modifier.height(16.dp))
                                        Text(
                                            text = "Evaluating your answer...",
                                            style = MaterialTheme.typography.bodyLarge,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text(
                                            text = "Analyzing clarity, logic, perspective, and depth",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Evaluation results
                    item {
                        uiState.evaluation?.let { evaluation ->
                            ModernCard(
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(
                                    modifier = Modifier.padding(20.dp)
                                ) {
                                    Text(
                                        text = "Evaluation Results",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Medium
                                    )
                                    Spacer(modifier = Modifier.height(16.dp))
                                    
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        ModernScoreCard(
                                            title = "Clarity",
                                            score = evaluation.clarityScore,
                                            modifier = Modifier.weight(1f),
                                            color = accent_blue
                                        )
                                        ModernScoreCard(
                                            title = "Logic",
                                            score = evaluation.logicScore,
                                            modifier = Modifier.weight(1f),
                                            color = accent_green
                                        )
                                    }
                                    
                                    Spacer(modifier = Modifier.height(8.dp))
                                    
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        ModernScoreCard(
                                            title = "Perspective",
                                            score = evaluation.perspectiveScore,
                                            modifier = Modifier.weight(1f),
                                            color = accent_orange
                                        )
                                        ModernScoreCard(
                                            title = "Depth",
                                            score = evaluation.depthScore,
                                            modifier = Modifier.weight(1f),
                                            color = accent_purple
                                        )
                                    }
                                    
                                    Spacer(modifier = Modifier.height(16.dp))
                                    ModernDivider()
                                    Spacer(modifier = Modifier.height(16.dp))
                                    
                                    Text(
                                        text = "Feedback",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Medium
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = evaluation.feedback,
                                        style = MaterialTheme.typography.bodyMedium,
                                        lineHeight = MaterialTheme.typography.bodyMedium.lineHeight * 1.2f
                                    )
                                }
                            }
                        }
                    }

                    // Word and Phrase Suggestions
                    item {
                        uiState.evaluation?.let { evaluation ->
                            if (evaluation.wordAndPhraseSuggestions.isNotBlank()) {
                                ModernCard(
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(
                                        modifier = Modifier.padding(20.dp)
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                Icons.Default.Edit,
                                                contentDescription = "Word Suggestions",
                                                tint = MaterialTheme.colorScheme.secondary,
                                                modifier = Modifier.size(24.dp)
                                            )
                                            Spacer(modifier = Modifier.width(12.dp))
                                            Text(
                                                text = "Word & Phrase Suggestions",
                                                style = MaterialTheme.typography.titleMedium,
                                                fontWeight = FontWeight.Medium
                                            )
                                        }
                                        
                                        Spacer(modifier = Modifier.height(16.dp))
                                        
                                        Text(
                                            text = evaluation.wordAndPhraseSuggestions,
                                            style = MaterialTheme.typography.bodyMedium,
                                            lineHeight = MaterialTheme.typography.bodyMedium.lineHeight * 1.2f
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Better Answer Suggestions
                    item {
                        uiState.evaluation?.let { evaluation ->
                            if (evaluation.betterAnswerSuggestions.isNotBlank()) {
                                ModernCard(
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(
                                        modifier = Modifier.padding(20.dp)
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                Icons.Default.Create,
                                                contentDescription = "Better Answers",
                                                tint = MaterialTheme.colorScheme.tertiary,
                                                modifier = Modifier.size(24.dp)
                                            )
                                            Spacer(modifier = Modifier.width(12.dp))
                                            Text(
                                                text = "Better Answer Suggestions",
                                                style = MaterialTheme.typography.titleMedium,
                                                fontWeight = FontWeight.Medium
                                            )
                                        }
                                        
                                        Spacer(modifier = Modifier.height(16.dp))
                                        
                                        Text(
                                            text = evaluation.betterAnswerSuggestions,
                                            style = MaterialTheme.typography.bodyMedium,
                                            lineHeight = MaterialTheme.typography.bodyMedium.lineHeight * 1.2f
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Thought Process Guidance
                    item {
                        uiState.evaluation?.let { evaluation ->
                            ModernCard(
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(
                                    modifier = Modifier.padding(20.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            Icons.Default.Edit,
                                            contentDescription = "Thought Process",
                                            tint = MaterialTheme.colorScheme.tertiary,
                                            modifier = Modifier.size(24.dp)
                                        )
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Text(
                                            text = "Thought Process Guidance",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                    
                                    Spacer(modifier = Modifier.height(16.dp))
                                    
                                    Text(
                                        text = evaluation.thoughtProcessGuidance,
                                        style = MaterialTheme.typography.bodyMedium,
                                        lineHeight = MaterialTheme.typography.bodyMedium.lineHeight * 1.2f
                                    )
                                }
                            }
                        }
                    }

                    // Ask Another Question button after feedback
                    item {
                        uiState.evaluation?.let { evaluation ->
                            ModernButton(
                                onClick = { onEvent(MainUiEvent.GenerateQuestion) },
                                isLoading = uiState.isGeneratingQuestion,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(Icons.Default.Refresh, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Ask Another Question")
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun EvaluationResults(
    evaluation: AnswerEvaluation,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Evaluation Results",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Scores
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                ScoreCard(
                    title = "Clarity",
                    score = evaluation.clarityScore,
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(8.dp))
                ScoreCard(
                    title = "Logic",
                    score = evaluation.logicScore,
                    modifier = Modifier.weight(1f)
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                ScoreCard(
                    title = "Perspective",
                    score = evaluation.perspectiveScore,
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(8.dp))
                ScoreCard(
                    title = "Depth",
                    score = evaluation.depthScore,
                    modifier = Modifier.weight(1f)
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Feedback
            Text(
                text = "Feedback",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = evaluation.feedback,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Model answer
            Text(
                text = "Model Answer",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = evaluation.modelAnswer,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
        }
    }
}

@Composable
fun HistoryItem(
    questionWithAnswer: QuestionWithAnswer,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = questionWithAnswer.question.text,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.fillMaxWidth()
            )
            
            questionWithAnswer.answer?.let { answer ->
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Your Answer: ${answer.userAnswer}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Scores: C${answer.clarityScore} L${answer.logicScore} P${answer.perspectiveScore} D${answer.depthScore}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}
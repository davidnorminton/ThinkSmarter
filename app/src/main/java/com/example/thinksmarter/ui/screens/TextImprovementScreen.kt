package com.example.thinksmarter.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.thinksmarter.ui.components.*
import com.example.thinksmarter.ui.theme.*
import com.example.thinksmarter.ui.viewmodel.TextImprovementUiEvent
import com.example.thinksmarter.ui.viewmodel.TextImprovementUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TextImprovementScreen(
    uiState: TextImprovementUiState,
    onEvent: (TextImprovementUiEvent) -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Text Improvement") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                    navigationIconContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Error message
            uiState.error?.let { error ->
                item {
                    ModernCard(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Warning,
                                contentDescription = "Error",
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = error,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            }

            // Text input section
            item {
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
                            contentDescription = "Text Input",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Enter Your Text",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Text(
                        text = "Text Type",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    LazyRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(horizontal = 4.dp)
                    ) {
                        val textTypes = listOf("General", "Academic", "Business", "Creative", "Technical", "Persuasive")
                        items(textTypes) { textType ->
                            ModernChip(
                                text = textType,
                                onClick = { onEvent(TextImprovementUiEvent.UpdateTextType(textType)) },
                                selected = uiState.selectedTextType == textType
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    val focusRequester = remember { FocusRequester() }
                    LaunchedEffect(Unit) {
                        focusRequester.requestFocus()
                    }
                    
                    ModernTextField(
                        value = uiState.userText,
                        onValueChange = { onEvent(TextImprovementUiEvent.UpdateUserText(it)) },
                        label = "Enter your text here...",
                        minLines = 4,
                        maxLines = 8,
                        modifier = Modifier
                            .fillMaxWidth()
                            .focusRequester(focusRequester)
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    ModernButton(
                        text = "Analyze & Improve",
                        onClick = { onEvent(TextImprovementUiEvent.AnalyzeText) },
                        isLoading = uiState.isAnalyzing,
                        enabled = uiState.userText.isNotBlank() && !uiState.isAnalyzing,
                        modifier = Modifier.fillMaxWidth(),
                        icon = { Icon(Icons.Default.Create, contentDescription = null) }
                    )
                }
            }
        }

            // Loading state
            if (uiState.isAnalyzing) {
                item {
                    ModernCard(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp)
                                .padding(20.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                CircularProgressIndicator(
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(48.dp)
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = "Analyzing your text...",
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

            // Analysis results
            uiState.evaluation?.let { evaluation ->
                item {
                    ModernCard(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                    Column(
                        modifier = Modifier.padding(20.dp)
                    ) {
                        Text(
                            text = "Analysis Results",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Medium
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        // Scores
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
                        
                        // Feedback Section
                        if (evaluation.feedback.isNotEmpty()) {
                            Text(
                                text = "Feedback",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Medium
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = evaluation.feedback,
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                        } else {
                            Text(
                                text = "No feedback available",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                        }
                        
                        // Word and Phrase Suggestions Section
                        if (evaluation.wordAndPhraseSuggestions.isNotEmpty()) {
                            Text(
                                text = "Word & Phrase Suggestions",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Medium
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = evaluation.wordAndPhraseSuggestions,
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                        }
                        
                        // Better Answer Suggestions Section
                        if (evaluation.betterAnswerSuggestions.isNotEmpty()) {
                            Text(
                                text = "Improvement Suggestions",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Medium
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = evaluation.betterAnswerSuggestions,
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                        }
                        
                        // Thought Process Guidance Section
                        if (evaluation.thoughtProcessGuidance.isNotEmpty()) {
                            Text(
                                text = "Writing Process Guidance",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Medium
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = evaluation.thoughtProcessGuidance,
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                        }
                        
                        // Model Answer Section
                        if (evaluation.modelAnswer.isNotEmpty()) {
                            ModernDivider()
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "Improved Version",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Medium
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = evaluation.modelAnswer,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
            }
        }
    }
}
}
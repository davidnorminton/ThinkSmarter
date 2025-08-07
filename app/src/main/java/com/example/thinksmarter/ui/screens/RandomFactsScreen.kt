package com.example.thinksmarter.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.thinksmarter.ui.components.ModernCard
import com.example.thinksmarter.ui.components.ModernChip
import com.example.thinksmarter.ui.components.ModernButton
import com.example.thinksmarter.ui.components.ErrorMessage
import com.example.thinksmarter.ui.viewmodel.RandomFactsUiState
import com.example.thinksmarter.ui.viewmodel.RandomFactsUiEvent

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RandomFactsScreen(
    uiState: RandomFactsUiState,
    onEvent: (RandomFactsUiEvent) -> Unit,
    onNavigateBack: () -> Unit,
    onNavigateToPreviousFacts: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Random Facts") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
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
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header
            item {
                Text(
                    "Discover Amazing Facts",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            
            item {
                Text(
                    "Learn something new from different categories",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            // Category Selection
            item {
                ModernCard {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            "Select Category",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(uiState.availableCategories) { category ->
                                ModernChip(
                                    text = category.name,
                                    selected = category.name == uiState.selectedCategory,
                                    onClick = { onEvent(RandomFactsUiEvent.SelectCategory(category.name)) }
                                )
                            }
                        }
                    }
                }
            }
            
            // Generate Button
            item {
                ModernButton(
                    onClick = { onEvent(RandomFactsUiEvent.GenerateRandomFact) },
                    enabled = !uiState.isLoading,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (uiState.isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = MaterialTheme.colorScheme.onPrimary,
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Generating...")
                    } else {
                        Icon(Icons.Default.Info, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Generate Random Fact")
                    }
                }
            }
            
            // Error Display
            uiState.error?.let { error ->
                item {
                    ErrorMessage(
                        message = error,
                        onDismiss = { onEvent(RandomFactsUiEvent.ClearError) }
                    )
                }
            }
            
            // Fact Display
            uiState.currentFact?.let { fact ->
                item {
                    ModernCard {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    Icons.Default.Info,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    "Random Fact",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            
                            Text(
                                fact,
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurface,
                                textAlign = TextAlign.Start
                            )
                            
                            // Refresh button for the same fact
                            ModernButton(
                                onClick = { onEvent(RandomFactsUiEvent.GenerateRandomFact) },
                                enabled = !uiState.isLoading,
                                modifier = Modifier.align(Alignment.End)
                            ) {
                                Icon(Icons.Default.Refresh, contentDescription = null)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("New Fact")
                            }
                        }
                    }
                }
            }
            
            // View Previous Facts Button
            item {
                ModernButton(
                    onClick = onNavigateToPreviousFacts,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Info, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("View Previous Facts")
                }
            }
            
            // Empty state when no fact is generated
            if (uiState.currentFact == null && !uiState.isLoading && uiState.error == null) {
                item {
                    ModernCard {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Icon(
                                Icons.Default.Info,
                                contentDescription = null,
                                modifier = Modifier.size(48.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            
                            Text(
                                "Ready to Learn?",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            
                            Text(
                                "Select a category and tap 'Generate Random Fact' to discover something amazing!",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }
        }
    }
} 
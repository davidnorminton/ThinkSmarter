package com.example.thinksmarter.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import coil.compose.AsyncImage
import com.example.thinksmarter.data.auth.UserProfile

data class DrawerMenuItem(
    val title: String,
    val icon: @Composable () -> Unit,
    val onClick: () -> Unit
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DrawerMenu(
    onNavigateToSettings: () -> Unit,
    onNavigateToStatistics: () -> Unit,
    onNavigateToDailyChallenge: () -> Unit,
    onNavigateToRecentQuestions: () -> Unit,
    onNavigateToTextImprovement: () -> Unit,
    onNavigateToUserProfile: () -> Unit,
    onClose: () -> Unit,
    userProfile: UserProfile? = null,
    modifier: Modifier = Modifier
) {
    ModalDrawerSheet(modifier = modifier) {
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "ThinkSmarter",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(16.dp)
        )
        HorizontalDivider()
        
        val menuItems = listOf(
            DrawerMenuItem(
                title = "Recent Questions",
                icon = { Icon(Icons.AutoMirrored.Filled.List, contentDescription = null) },
                onClick = {
                    onClose()
                    onNavigateToRecentQuestions()
                }
            ),
            DrawerMenuItem(
                title = "Text Improvement",
                icon = { Icon(Icons.Default.Edit, contentDescription = null) },
                onClick = {
                    onClose()
                    onNavigateToTextImprovement()
                }
            ),
            DrawerMenuItem(
                title = "Statistics",
                icon = { Icon(Icons.Default.Search, contentDescription = "Statistics") },
                onClick = {
                    onClose()
                    onNavigateToStatistics()
                }
            ),
            DrawerMenuItem(
                title = "Daily Challenge",
                icon = { Icon(Icons.Default.DateRange, contentDescription = null) },
                onClick = {
                    onClose()
                    onNavigateToDailyChallenge()
                }
            ),
            DrawerMenuItem(
                title = "Settings",
                icon = { Icon(Icons.Default.Settings, contentDescription = null) },
                onClick = {
                    onClose()
                    onNavigateToSettings()
                }
            )
        )

        menuItems.forEach { item ->
            NavigationDrawerItem(
                label = { Text(item.title) },
                icon = item.icon,
                selected = false,
                onClick = item.onClick,
                modifier = Modifier.padding(horizontal = 12.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
        }
        
        // Add spacer to push user profile to bottom
        Spacer(modifier = Modifier.weight(1f))
        
        // User profile section at bottom
        HorizontalDivider()
        NavigationDrawerItem(
            label = { Text(if (userProfile != null) userProfile.name else "Profile") },
            icon = {
                if (userProfile?.photoUrl != null) {
                    AsyncImage(
                        model = userProfile.photoUrl,
                        contentDescription = "Profile photo",
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Icon(Icons.Default.Person, contentDescription = null)
                }
            },
            selected = false,
            onClick = {
                onClose()
                onNavigateToUserProfile()
            },
            modifier = Modifier.padding(horizontal = 12.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
    }
}
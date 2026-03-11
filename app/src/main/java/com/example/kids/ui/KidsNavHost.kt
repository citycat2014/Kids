package com.example.kids.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.kids.ui.kid.KidListViewModel
import com.example.kids.ui.screens.GrowthRecordScreen
import com.example.kids.ui.screens.GrowthTimelineScreen
import com.example.kids.ui.screens.KidDetailScreen
import com.example.kids.ui.screens.KidListScreen
import com.example.kids.ui.screens.MoodCalendarScreen

object KidsDestinations {
    const val KID_LIST = "kid_list"
    const val KID_DETAIL = "kid_detail"
    const val GROWTH = "growth"
    const val MOOD = "mood"
    const val GROWTH_TIMELINE = "growth_timeline"
}

@Composable
fun KidsNavHost(
    navController: NavHostController = rememberNavController()
) {
    NavHost(
        navController = navController,
        startDestination = KidsDestinations.KID_LIST,
        modifier = Modifier.fillMaxSize()
    ) {
        composable(KidsDestinations.KID_LIST) {
            val vm: KidListViewModel = viewModel()
            KidListScreen(
                kids = vm.kids.collectAsState().value,
                onAddKid = { navController.navigate(KidsDestinations.KID_DETAIL) },
                onViewGrowth = { id ->
                    navController.navigate("${KidsDestinations.GROWTH}/$id")
                },
                onViewMood = { id ->
                    navController.navigate("${KidsDestinations.MOOD}/$id")
                },
                onEditKid = { id ->
                    navController.navigate("${KidsDestinations.KID_DETAIL}/$id")
                }
            )
        }

        composable(KidsDestinations.KID_DETAIL) {
            KidDetailScreen(
                kidId = null,
                onFinished = { navController.popBackStack() },
                onOpenGrowth = { id ->
                    navController.navigate("${KidsDestinations.GROWTH}/$id")
                },
                onOpenMood = { id ->
                    navController.navigate("${KidsDestinations.MOOD}/$id")
                }
            )
        }

        composable("${KidsDestinations.KID_DETAIL}/{kidId}") { backStackEntry ->
            val kidId = backStackEntry.arguments?.getString("kidId")?.toLongOrNull()
            KidDetailScreen(
                kidId = kidId,
                onFinished = { navController.popBackStack() },
                onOpenGrowth = { id ->
                    navController.navigate("${KidsDestinations.GROWTH}/$id")
                },
                onOpenMood = { id ->
                    navController.navigate("${KidsDestinations.MOOD}/$id")
                }
            )
        }

        composable("${KidsDestinations.GROWTH}/{kidId}") { backStackEntry ->
            val kidId = backStackEntry.arguments?.getString("kidId")?.toLongOrNull() ?: return@composable
            GrowthRecordScreen(
                kidId = kidId,
                onBack = { navController.popBackStack() },
                onOpenTimeline = { id ->
                    navController.navigate("${KidsDestinations.GROWTH_TIMELINE}/$id")
                }
            )
        }

        composable("${KidsDestinations.GROWTH_TIMELINE}/{kidId}") { backStackEntry ->
            val kidId = backStackEntry.arguments?.getString("kidId")?.toLongOrNull() ?: return@composable
            GrowthTimelineScreen(
                kidId = kidId,
                onBack = { navController.popBackStack() }
            )
        }

        composable("${KidsDestinations.MOOD}/{kidId}") { backStackEntry ->
            val kidId = backStackEntry.arguments?.getString("kidId")?.toLongOrNull() ?: return@composable
            MoodCalendarScreen(
                kidId = kidId,
                onBack = { navController.popBackStack() }
            )
        }
    }
}


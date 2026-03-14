package com.example.kids.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.kids.ui.kid.KidListViewModel
import com.example.kids.ui.academic.AcademicRecordScreen
import com.example.kids.ui.screens.GrowthRecordScreen
import com.example.kids.ui.screens.GrowthStandardScreen
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
    const val GROWTH_STANDARD = "growth_standard"
    const val ACADEMIC = "academic"
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
                },
                // 学习档案入口
                onViewAcademic = { id, grade ->
                    if (!grade.isNullOrBlank()) {
                        navController.navigate("${KidsDestinations.ACADEMIC}/$id?grade=$grade")
                    } else {
                        navController.navigate("${KidsDestinations.ACADEMIC}/$id")
                    }
                }
            )
        }

        composable(KidsDestinations.KID_DETAIL) {
            KidDetailScreen(
                kidId = null,
                onFinished = { navController.popBackStack() },
                onCancel = { navController.popBackStack() }
            )
        }

        composable("${KidsDestinations.KID_DETAIL}/{kidId}") { backStackEntry ->
            val kidId = backStackEntry.arguments?.getString("kidId")?.toLongOrNull()
            KidDetailScreen(
                kidId = kidId,
                onFinished = { navController.popBackStack() },
                onCancel = { navController.popBackStack() }
            )
        }

        composable("${KidsDestinations.GROWTH}/{kidId}") { backStackEntry ->
            val kidId = backStackEntry.arguments?.getString("kidId")?.toLongOrNull() ?: return@composable
            GrowthRecordScreen(
                kidId = kidId,
                onBack = { navController.popBackStack() },
                onOpenTimeline = { id ->
                    navController.navigate("${KidsDestinations.GROWTH_TIMELINE}/$id")
                },
                onOpenStandard = { gender, age ->
                    navController.navigate(
                        "${KidsDestinations.GROWTH_STANDARD}/$kidId?gender=$gender&age=$age"
                    )
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

        composable(
            route = "${KidsDestinations.GROWTH_STANDARD}/{kidId}?gender={gender}&age={age}",
            arguments = listOf(
                navArgument("gender") {
                    type = NavType.StringType
                    defaultValue = "男"
                },
                navArgument("age") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                }
            )
        ) { backStackEntry ->
            val kidGender = backStackEntry.arguments?.getString("gender") ?: "男"
            val currentAge = backStackEntry.arguments?.getString("age")?.toIntOrNull()
            GrowthStandardScreen(
                kidGender = kidGender,
                currentAge = currentAge,
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

        composable(
            route = "${KidsDestinations.ACADEMIC}/{kidId}?grade={grade}",
            arguments = listOf(
                navArgument("kidId") { type = NavType.LongType },
                navArgument("grade") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                }
            )
        ) { backStackEntry ->
            val kidId = backStackEntry.arguments?.getLong("kidId") ?: return@composable
            val grade = backStackEntry.arguments?.getString("grade")
            AcademicRecordScreen(
                kidId = kidId,
                initialGrade = grade,
                onBack = { navController.popBackStack() }
            )
        }
    }
}


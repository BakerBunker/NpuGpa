package com.bakerbunker.npugpa.ui

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.rememberScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.navigation.compose.rememberNavController
import com.bakerbunker.npugpa.ui.theme.NpugpaTheme
import com.google.accompanist.systemuicontroller.rememberSystemUiController

@ExperimentalFoundationApi
@ExperimentalAnimationApi
@Composable
fun NpuGpaApp(){
    val navController= rememberNavController()
    val scaffoldState= rememberScaffoldState()

    val systemUiController= rememberSystemUiController()
    val primaryColor=MaterialTheme.colors.primary
    val darkIcons = MaterialTheme.colors.isLight
    NpugpaTheme {
        SideEffect {
            systemUiController.setNavigationBarColor(primaryColor)
            systemUiController.setStatusBarColor(
                color = Color.Transparent,
                darkIcons
            )
        }

        Scaffold(
            scaffoldState=scaffoldState,
        ) {
            NpuGpaNavGraph(navController,scaffoldState)
        }
    }
}
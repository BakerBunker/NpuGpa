package com.bakerbunker.npugpa.ui

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.material.ScaffoldState
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.bakerbunker.npugpa.ui.displaygpa.GpaDisplayScreen
import com.bakerbunker.npugpa.ui.login.LoginScreen

object MainDestinations{
    const val LOGIN_ROUTE="login"
    const val DISPLAY_ROUTE="display"
}

@ExperimentalFoundationApi
@ExperimentalAnimationApi
@Composable
fun NpuGpaNavGraph(
    navController: NavHostController,
    scaffoldState: ScaffoldState
){
    NavHost(navController = navController,startDestination = MainDestinations.LOGIN_ROUTE){
        composable(MainDestinations.LOGIN_ROUTE){
            LoginScreen(navController,scaffoldState)
        }
        composable(MainDestinations.DISPLAY_ROUTE){
            GpaDisplayScreen(navController,scaffoldState)
        }
    }
}
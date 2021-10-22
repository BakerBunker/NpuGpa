package com.bakerbunker.npugpa.ui

import androidx.compose.animation.*
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.material.ScaffoldState
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import com.google.accompanist.navigation.animation.composable
import com.bakerbunker.npugpa.ui.displaygpa.GpaDisplayScreen
import com.bakerbunker.npugpa.ui.login.LoginScreen
import com.google.accompanist.navigation.animation.AnimatedNavHost

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
    AnimatedNavHost(navController = navController,startDestination = MainDestinations.LOGIN_ROUTE){
        composable(
            MainDestinations.LOGIN_ROUTE){
            LoginScreen(navController,scaffoldState)
        }
        composable(MainDestinations.DISPLAY_ROUTE,enterTransition = {_,_-> fadeIn()+slideInHorizontally(initialOffsetX = {1000})},exitTransition = {_,_-> fadeOut()+slideOutHorizontally(targetOffsetX = {-1000}) }){
            GpaDisplayScreen(navController)
        }
    }
}
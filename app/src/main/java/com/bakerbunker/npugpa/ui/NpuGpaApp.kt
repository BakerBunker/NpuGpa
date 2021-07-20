package com.bakerbunker.npugpa.ui

import android.app.Application
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.rememberScaffoldState
import androidx.compose.runtime.Composable
import androidx.navigation.compose.rememberNavController
import com.bakerbunker.npugpa.NpuGpaApplication
import com.bakerbunker.npugpa.ui.theme.NpugpaTheme

@ExperimentalFoundationApi
@ExperimentalAnimationApi
@Composable
fun NpuGpaApp(){
    val navController= rememberNavController()
    val scaffoldState= rememberScaffoldState()
    NpugpaTheme {
        Scaffold(
            scaffoldState=scaffoldState,
        ) {
            NpuGpaNavGraph(navController,scaffoldState)
        }
    }
}
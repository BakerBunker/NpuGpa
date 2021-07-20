package com.bakerbunker.npugpa

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.datastore.preferences.core.edit
import androidx.lifecycle.lifecycleScope
import com.bakerbunker.npugpa.ui.NpuGpaApp
import com.bakerbunker.npugpa.util.SELECTED
import com.bakerbunker.npugpa.util.dataStore
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    private val viewModel by viewModels<MainViewModel>()

    @ExperimentalAnimationApi
    @ExperimentalFoundationApi
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            NpuGpaApp()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        lifecycleScope.launch{
            viewModel.storeSelectedCourseNums { selected->
                (this as Context).dataStore.edit {
                    it[SELECTED]=selected
                }
            }
        }
    }
}


package com.bakerbunker.npugpa.ui.login

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.datastore.preferences.core.edit
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.bakerbunker.npugpa.MainViewModel
import com.bakerbunker.npugpa.R
import com.bakerbunker.npugpa.ui.MainDestinations
import com.bakerbunker.npugpa.util.ACCOUNT
import com.bakerbunker.npugpa.util.PASSWORD
import com.bakerbunker.npugpa.util.dataStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun LoginScreen(navController: NavHostController, scaffoldState: ScaffoldState) {
    val context= LocalContext.current
    val accountText = remember {
        mutableStateOf("")
    }
    val passwordText = remember {
        mutableStateOf("")
    }
    val isLoading = remember {
        mutableStateOf(false)
    }
    val isRemembered= remember {
        mutableStateOf(false)
    }
    val viewModel = viewModel<MainViewModel>()
    val snackbarCoroutineScope = rememberCoroutineScope()
    val ioScope = rememberCoroutineScope()

    LaunchedEffect(key1 = true){
        accountText.value=context.dataStore.data.map {
            it[ACCOUNT]?:""
        }.first()
        passwordText.value=context.dataStore.data.map {
            it[PASSWORD]?:""
        }.first()
        if(passwordText.value.isNotEmpty()){
            isRemembered.value=true
        }
    }
    BoxWithConstraints {
        val height=this.maxHeight
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(height/16,alignment = Alignment.CenterVertically),
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(stringResource(R.string.login_header), style = MaterialTheme.typography.h3)
            //Spacer(modifier = Modifier.padding(32.dp))
            LoginAccountField(account = accountText)
            //Spacer(modifier = Modifier.padding(32.dp))
            LoginPasswordField(
                password = passwordText,
                isRemembered = isRemembered.value,
                changeRemembered = { isRemembered.value = !isRemembered.value })
            //Spacer(modifier = Modifier.padding(32.dp))
            if (!isLoading.value) {
                OutlinedButton(
                    onClick = {
                        isLoading.value = true
                        ioScope.launch {
                            context.dataStore.edit {
                                it[ACCOUNT] = accountText.value
                            }
                            context.dataStore.edit {
                                it[PASSWORD] = passwordText.value
                            }
                            viewModel.login(
                                account = accountText.value,
                                password = passwordText.value,
                                onError = {
                                    snackbarCoroutineScope.launch {
                                        isLoading.value = false
                                        scaffoldState.snackbarHostState.showSnackbar(it)
                                    }
                                },
                                onSuccess = {
                                    isLoading.value = false
                                    withContext(Dispatchers.Main) {
                                        navController.navigate(MainDestinations.DISPLAY_ROUTE)
                                    }
                                })
                        }
                    }
                ) {
                    Text(stringResource(R.string.login_lowercase))
                }
            } else {
                CircularProgressIndicator()
            }
        }
    }
}
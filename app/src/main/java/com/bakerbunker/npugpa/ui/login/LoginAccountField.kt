package com.bakerbunker.npugpa.ui.login

import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.bakerbunker.npugpa.R

@Composable
fun LoginAccountField(account:MutableState<String>){
    OutlinedTextField(
        value = account.value,
        onValueChange = {account.value=it},
        label = { Text(text = stringResource(R.string.student_number)) },
        singleLine = true,
    )
}

@Preview(showBackground = true)
@Composable
private fun LoginAccountFieldPreview(){
    val account = remember {
        mutableStateOf("")
    }
    LoginAccountField(account = account)
}
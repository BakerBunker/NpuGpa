package com.bakerbunker.npugpa.ui.login

import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.RemoveRedEye
import androidx.compose.material.icons.outlined.RemoveRedEye
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import com.bakerbunker.npugpa.ui.login.LoginAccountField

@Composable
fun LoginPasswordField(password: MutableState<String>,isRemembered:Boolean,changeRemembered:()->Unit) {
    val visibility = remember {
        mutableStateOf(false)
    }

    OutlinedTextField(
        value = password.value,
        onValueChange = { password.value = it },
        label = { Text(text = "password") },
        singleLine = true,
        keyboardOptions= KeyboardOptions(keyboardType = KeyboardType.Password),
        visualTransformation = if (visibility.value) VisualTransformation.None else PasswordVisualTransformation(),
        trailingIcon = {
            if(isRemembered){
                IconButton(onClick = {
                    password.value=""
                    changeRemembered()
                }) {
                    Icon(Icons.Filled.Cancel,null)
                }
            }else{
                IconButton(
                    onClick = {
                        visibility.value = !visibility.value
                    })
                {
                    if (visibility.value) Icon(
                        Icons.Filled.RemoveRedEye,
                        null
                    ) else Icon(Icons.Outlined.RemoveRedEye, null)
                }
            }
        },
    )
}
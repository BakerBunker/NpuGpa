package com.bakerbunker.npugpa.ui.component

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.MaterialTheme
import androidx.compose.material.primarySurface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun StyledTile(modifier: Modifier=Modifier,isSelected:Boolean,content:@Composable ()->Unit){
    Card(
        elevation = 0.dp,
        modifier = modifier,
        backgroundColor = if(isSelected) MaterialTheme.colors.primaryVariant else MaterialTheme.colors.primarySurface,
        shape = RoundedCornerShape(0.dp),
        content = content
    )
}
package com.bakerbunker.npugpa.ui.component

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.MaterialTheme
import androidx.compose.material.primarySurface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp

@Composable
fun StyledCard(
    modifier: Modifier=Modifier,
    shape: Shape=RoundedCornerShape(16.dp),
    backgroundColor:Color=MaterialTheme.colors.primarySurface,
    content:@Composable ()->Unit){
    Card(
        modifier = modifier
            //.padding(horizontal = 16.dp, vertical = 12.dp)
            .fillMaxWidth(),
        elevation = 10.dp,
        backgroundColor = backgroundColor,
        shape = shape,
        content = content
    )
}
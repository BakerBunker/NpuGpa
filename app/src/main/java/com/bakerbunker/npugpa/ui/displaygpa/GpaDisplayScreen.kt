package com.bakerbunker.npugpa.ui.displaygpa

import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.compose.LocalOnBackPressedDispatcherOwner
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.datastore.preferences.core.edit
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.bakerbunker.npugpa.MainViewModel
import com.bakerbunker.npugpa.R
import com.bakerbunker.npugpa.model.Course
import com.bakerbunker.npugpa.ui.component.StyledCard
import com.bakerbunker.npugpa.ui.component.StyledTile
import com.bakerbunker.npugpa.util.SELECTED
import com.bakerbunker.npugpa.util.dataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

val SHOW_SCORE = mutableStateOf(false)

@ExperimentalFoundationApi
@ExperimentalAnimationApi
@Composable
fun GpaDisplayScreen(navController: NavController) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val viewModel = viewModel<MainViewModel>()
    val isCompleted = remember { mutableStateOf(false) }
    rememberCoroutineScope()
    val totalSum = viewModel.totalSum.observeAsState()

    LaunchedEffect(true) {
        val selectedCourses = context.dataStore.data.map {
            it[SELECTED] ?: ""
        }.first().split(" ").toSet()
        viewModel.queryGpa(
            onSuccess = {
                isCompleted.value = true
            },
            selectedCourses = selectedCourses
        )
    }

    val backDispatcher = LocalOnBackPressedDispatcherOwner.current?.onBackPressedDispatcher
    val backCallback = remember {
        object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (isEnabled) {
                    viewModel.storeSelectedCourseNums { selected ->
                        context.dataStore.edit {
                            it[SELECTED] = selected
                        }
                    }
                    Toast.makeText(context, context.getString(R.string.press_back_again), Toast.LENGTH_SHORT).show()
                    isEnabled = false
                } else {
                    navController.popBackStack()
                }
            }
        }
    }
    DisposableEffect(backDispatcher) {
        // Add callback to the backDispatcher
        backDispatcher?.addCallback(backCallback)
        // When the effect leaves the Composition, remove the callback
        onDispose {
            backCallback.remove()
        }
    }


    if (!isCompleted.value) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
    } else {
        Column {
            //AnimatedVisibility(visible = totalSum.value!!.totalCredit != 0.0) {
            StyledCard(
                shape = RoundedCornerShape(bottomEnd = 16.dp, bottomStart = 16.dp),
                backgroundColor = MaterialTheme.colors.primaryVariant,
                modifier = Modifier.combinedClickable(
                    onLongClick = {
                        viewModel.changeAllState()
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    },
                    onClick = {}
                )
            ) {
                Column(
                    modifier = Modifier
                        .padding(16.dp)
                        .padding(top = 16.dp)
                ) {
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .padding(16.dp)
                            .fillMaxWidth()
                    ) {
                        Text(
                            text = stringResource(R.string.summary),
                            style = MaterialTheme.typography.h5,
                        )
                        Row {
                            Text(text = stringResource(R.string.show_score))
                            Spacer(modifier = Modifier.width(8.dp))
                            Switch(
                                checked = SHOW_SCORE.value,
                                onCheckedChange = { SHOW_SCORE.value = it })
                        }
                    }
                    StyledTile(
                        isSelected = true, modifier = Modifier
                            .padding(horizontal = 16.dp)
                            .fillMaxWidth()
                    ) {
                        Column {
                            Text(
                                text = stringResource(R.string.total_credit)+' ' + totalSum.value!!.totalCredit,
                                style = MaterialTheme.typography.subtitle2,
                            )
                            Text(
                                text = stringResource(R.string.avg_score)+' ' + totalSum.value!!.avgScore.orHide(),
                                style = MaterialTheme.typography.subtitle2,
                            )
                            Text(
                                text = stringResource(R.string.avg_gpa) +' '+ totalSum.value!!.avgGpa.orHide(),
                                style = MaterialTheme.typography.subtitle2,
                            )
                        }
                    }
                }
            }
            //}
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                //.padding(top = if (totalSum.value!!.totalCredit == 0.0) 32.dp else 0.dp)
                //.animateContentSize()
            ) {
                items(viewModel.courseList.groupBy { gpaItem ->
                    gpaItem.semesterName
                }.toList().reversed()) { (semesterName, courseList) ->
                    SemesterCard(semesterName = semesterName, courseList = courseList)
                }
                item {
                    Box(modifier = Modifier.size(25.dp))
                }
            }
        }
    }
}

@ExperimentalFoundationApi
@ExperimentalAnimationApi
@Composable
private fun SemesterCard(semesterName: String, courseList: List<Course>) {
    val viewModel = viewModel<MainViewModel>()
    val haptic = LocalHapticFeedback.current
    val semesterSum = viewModel.getSemesterSum(semesterName).observeAsState()

    StyledCard(
        modifier = Modifier
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .combinedClickable(
                onClick = {},
                onLongClick = {
                    viewModel.changeSemesterState(semesterName)
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                }
            ),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = semesterName,
                style = if (semesterSum.value?.totalCredit != 0.0) MaterialTheme.typography.h5 else MaterialTheme.typography.h4,
                modifier = Modifier
                    .padding(16.dp)
                    .animateContentSize()
            )
            AnimatedVisibility(visible = semesterSum.value?.totalCredit != 0.0) {
                Text(
                    text = "${stringResource(R.string.total_credit)} ${semesterSum.value!!.totalCredit}\n${stringResource(R.string.avg_score)} ${semesterSum.value!!.avgScore.orHide()}\n${stringResource(R.string.avg_gpa)} ${semesterSum.value!!.avgGpa.orHide()}",
                    //text = "Total credit ${semesterSum.value!!.totalCredit}\nAvg score ${semesterSum.value!!.avgScore}\nAvg gpa ${semesterSum.value!!.avgGpa}",
                    style = MaterialTheme.typography.subtitle2,
                    overflow = TextOverflow.Visible,
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .padding(bottom = 8.dp)
                        .animateContentSize()
                )
            }
            for (course in courseList) {
                CourseTile(course = course)
            }
        }
    }
}

@ExperimentalAnimationApi
@ExperimentalFoundationApi
@Composable
private fun CourseTile(course: Course) {
    val viewModel = viewModel<MainViewModel>()
    val isSelected = viewModel.isCourseSelected(course).observeAsState()
    val isExpanded = remember {
        mutableStateOf(false)
    }
    StyledTile(
        modifier = Modifier.combinedClickable(
            onClick = {
                viewModel.changeCourseState(course)
            },
            onLongClick = {
                isExpanded.value = !isExpanded.value
            }
        ),
        isSelected = isSelected.value ?: false
    ) {
        Column(modifier = Modifier.animateContentSize()) {
            Row(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier
                        .weight(1f, false)
                        .animateContentSize()
                ) {
                    Text(text = course.courseName, style = MaterialTheme.typography.body1)
                    Text(text = course.credit.toString(), style = MaterialTheme.typography.body2)
                }
                Text(
                    text = "${course.score.orHide()}[${course.point.orHide()}]", modifier = Modifier
                        .padding(start = 16.dp)
                        .animateContentSize()
                )
            }
            AnimatedVisibility(visible = isExpanded.value) {
                Column {
                    DetailTile("实验成绩", course.experiment)
                    DetailTile("平时成绩", course.usual)
                    DetailTile("期中成绩", course.midterm)
                    DetailTile("期末成绩", course.endterm)
                }
            }
        }
    }


}

@Composable
fun DetailTile(type: String, score: Double) {
    if (score != -1.0) {
        Row(
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 4.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = type, style = MaterialTheme.typography.body2)
            Text(text = score.orHide(), modifier = Modifier.padding(start = 16.dp))
        }
    }
}

private fun <T> T.orHide() = if (SHOW_SCORE.value) this.toString() else "*****"
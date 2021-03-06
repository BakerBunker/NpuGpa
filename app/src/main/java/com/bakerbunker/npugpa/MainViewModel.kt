package com.bakerbunker.npugpa

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.bakerbunker.npugpa.model.Course
import com.bakerbunker.npugpa.model.ScoreSum
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.FormBody
import okhttp3.Request
import org.jsoup.Jsoup
import java.io.IOException

private const val LOGIN_URL = "http://us.nwpu.edu.cn/eams/login.action"
private const val SCORE_URL =
    "http://us.nwpu.edu.cn/eams/teach/grade/course/person!historyCourseGrade.action"

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val client = getApplication<NpuGpaApplication>().client
    private val resources=getApplication<NpuGpaApplication>().resources

    private val _gpaList = mutableListOf<Course>()
    val courseList: List<Course> = _gpaList

    private val selectedCourse = mutableMapOf<String, MutableLiveData<Boolean>>()
    private val selectedSemester = mutableMapOf<String, MutableLiveData<Boolean>>()
    private val _totalSum = MutableLiveData(ScoreSum(0.0, 0.0, 0.0))
    val totalSum:LiveData<ScoreSum>
        get() = _totalSum
    private val semesterSum = mutableMapOf<String, MutableLiveData<ScoreSum>>()

    suspend fun login(
        account: String,
        password: String,
        onError: (String) -> Unit,
        onSuccess: suspend () -> Unit
    ) {
        _gpaList.clear()
        selectedCourse.clear()
        _totalSum.value = ScoreSum(0.0, 0.0, 0.0)
        semesterSum.clear()
        val loginRequest = Request.Builder().url(LOGIN_URL)
        if (account.isEmpty()) {
            onError(resources.getString(R.string.empty_student_number))
            return
        }
        if (password.isEmpty()) {
            onError(resources.getString(R.string.empty_password))
            return
        }
        val loginRequestBody = FormBody.Builder()
            .add("username", account)
            .add("password", password)
            .add("encodedPassword", "")
            .add("session_locale", "zh_CN")
            .build()
        withContext(Dispatchers.IO) {
            try{
                val response = client.newCall(loginRequest.post(loginRequestBody).build()).execute()
                if (response.isSuccessful) {
                    val resposeString = response.body?.string() ?: ""
                    with(resposeString) {
                        when {
                            contains("????????????") -> onError(resources.getString(R.string.wrong_password))
                            contains("???????????????") -> onError(resources.getString(R.string.invalid_account))
                            contains("??????????????????") -> onError(resources.getString(R.string.need_validation_code))
                            contains("?????????????????????") -> onError(resources.getString(R.string.user_locked))
                            else -> {
                                onSuccess()
                            }
                        }
                    }
                }
                response.close()
            }catch (e:IOException){
                onError(resources.getString(R.string.network_timeout))
            }
        }
    }

    suspend fun queryGpa(onSuccess: () -> Unit, selectedCourses: Set<String>) {
        val scoreQueryRequest = Request.Builder().url(SCORE_URL)
        val response =
            withContext(Dispatchers.IO) { client.newCall(scoreQueryRequest.build()).execute() }

        val body = withContext(Dispatchers.IO) {
            Jsoup.parse(response.body!!.string()).body()
        }
        val nameMap = mutableMapOf<String, Int>()
        val detailHead = body.select(".gridtable .gridhead tr")[1]
        detailHead!!.children().forEachIndexed { index, element ->
            nameMap[element.text().trim()] = index
        }
        val detailBody = body.select(".gridtable tbody tr:gt(1)")

        val tempGpaList = mutableListOf<Course>()
        withContext(Dispatchers.Default) {
            for (scoreEntry in detailBody) {
                if (scoreEntry.className() == "script") continue
                val scoreEntryChildren = scoreEntry.children()
                val course= Course(
                    scoreEntryChildren[nameMap["????????????"]!!].text(),
                    scoreEntryChildren[nameMap["????????????"]!!].text(),
                    scoreEntryChildren[nameMap["????????????"]!!].text().ifEmpty { scoreEntryChildren[nameMap["????????????"]!!].text() },
                    scoreEntryChildren[nameMap["????????????"]!!].text(),
                    scoreEntryChildren[nameMap["??????"]!!].text().toDouble(),
                    scoreEntryChildren[nameMap["??????"]!!].text(),
                    scoreEntryChildren[nameMap["??????"]!!].text().toDoubleOrNull() ?: 0.0,

                    scoreEntryChildren[nameMap["????????????"]!!].text().toDoubleOrNull() ?: -1.0,
                    scoreEntryChildren[nameMap["????????????"]!!].text().toDoubleOrNull() ?: -1.0,
                    scoreEntryChildren[nameMap["????????????"]!!].text().toDoubleOrNull() ?: -1.0,
                    scoreEntryChildren[nameMap["????????????"]!!].text().toDoubleOrNull() ?: -1.0,
                )
                tempGpaList.add(course)
                selectedCourse.putIfAbsent(
                    course.courseName,
                    MutableLiveData(false)
                )
                selectedSemester.putIfAbsent(
                    course.semesterName,
                    MutableLiveData(false)
                )
                semesterSum.putIfAbsent(
                    course.semesterName,
                    MutableLiveData(ScoreSum(0.0, 0.0, 0.0))
                )
                if(course.courseNum in selectedCourses){
                    withContext(Dispatchers.Main){
                        changeCourseState(course)
                    }
                }
            }
        }
        withContext(Dispatchers.IO) {
            response.close()
        }
        _gpaList.addAll(tempGpaList)
        onSuccess()
    }

    fun changeCourseState(course: Course) {
        selectCourse(course) || unselectCourse(course)
    }

    private fun selectCourse(course: Course): Boolean {
        if (selectedCourse[course.courseName]!!.value == true) return false
        _totalSum.value = _totalSum.value!!.plus(course)
        semesterSum[course.semesterName]!!.value =
            semesterSum[course.semesterName]!!.value!!.plus(course)
        selectedCourse[course.courseName]!!.value = true
        return true
    }

    private fun unselectCourse(course: Course): Boolean {
        if (selectedCourse[course.courseName]!!.value == false) return false
        _totalSum.value = _totalSum.value!!.minus(course)
        semesterSum[course.semesterName]!!.value =
            semesterSum[course.semesterName]!!.value!!.minus(course)
        selectedCourse[course.courseName]!!.value = false
        return true
    }

    fun changeSemesterState(semesterName: String) {
        if (semesterSum[semesterName]!!.value!!.totalCredit == 0.0) {
            courseList.filter { it.semesterName == semesterName && !(it.courseType.contains("??????")||it.score.contains("P")) }
                .forEach { course -> selectCourse(course) }
        } else {
            courseList.filter { it.semesterName == semesterName }
                .forEach { course -> unselectCourse(course) }
        }
    }

    fun changeAllState() {
        if (_totalSum.value?.totalCredit==0.0) {
            courseList.filter {!(it.courseType.contains("??????")||it.score.contains("P")) }
                .forEach { course -> selectCourse(course) }
        } else {
            courseList.forEach { course -> unselectCourse(course) }
        }
    }

    fun isCourseSelected(course: Course): LiveData<Boolean> {
        return selectedCourse[course.courseName] ?: MutableLiveData(false)
    }

    fun getSemesterSum(semesterName: String): LiveData<ScoreSum> {
        return semesterSum[semesterName] ?: MutableLiveData(ScoreSum(0.0, 0.0, 0.0))
    }

    fun storeSelectedCourseNums(store:suspend (String)->Unit){
        val nums=courseList.filter { selectedCourse[it.courseName]!!.value==true }.map { it.courseNum }.reduceOrNull{str1,str2->"$str1 $str2"}?:""
        viewModelScope.launch {
            store(nums)
        }
    }
}
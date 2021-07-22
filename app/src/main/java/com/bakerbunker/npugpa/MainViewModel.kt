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

private const val LOGIN_URL = "http://us.nwpu.edu.cn/eams/login.action"
private const val SCORE_URL =
    "http://us.nwpu.edu.cn/eams/teach/grade/course/person!historyCourseGrade.action"

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val client = getApplication<NpuGpaApplication>().client

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
            onError("Student number cannot be empty")
            return
        }
        if (password.isEmpty()) {
            onError("Password cannot be empty")
            return
        }
        withContext(Dispatchers.IO) {
            if (!client.newCall(loginRequest.build()).execute().isSuccessful) {
                onError("Network error")
            }
        }
        val loginRequestBody = FormBody.Builder()
            .add("username", account)
            .add("password", password)
            .add("encodedPassword", "")
            .add("session_locale", "zh_CN")
            .build()
        withContext(Dispatchers.IO) {
            val response = client.newCall(loginRequest.post(loginRequestBody).build()).execute()
            if (response.isSuccessful) {
                val resposeString=response.body?.string() ?: ""
                with(resposeString) {
                    when {
                        contains("密码错误") -> onError("Wrong Password")
                        contains("账户不存在") -> onError("Invalid account")
                        contains("验证码不正确") -> onError("Wrong validation code")
                        contains("用户已经被锁定")-> onError("User has been locked, please login later")
                        else -> {
                            onSuccess()
                        }
                    }
                }
            } else {
                onError("No network or timeout")
            }
            response.close()
        }
    }

    suspend fun queryGpa(onError: (String) -> Unit, onSuccess: () -> Unit,selectedCourses:Set<String>) {
        val scoreQueryRequest = Request.Builder().url(SCORE_URL)
        val response =
            withContext(Dispatchers.IO) { client.newCall(scoreQueryRequest.build()).execute() }
        if (!response.isSuccessful) {
            onError("Failed to retrieve data")
        }

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
                if (scoreEntry.className().equals("script")) continue
                val scoreEntryChildren = scoreEntry.children()
                val course= Course(
                    scoreEntryChildren[nameMap["学年学期"]!!].text(),
                    scoreEntryChildren[nameMap["课程名称"]!!].text(),
                    scoreEntryChildren[nameMap["课程序号"]!!].text().ifEmpty { scoreEntryChildren[nameMap["课程代码"]!!].text() },
                    scoreEntryChildren[nameMap["课程类别"]!!].text(),
                    scoreEntryChildren[nameMap["学分"]!!].text().toDouble(),
                    scoreEntryChildren[nameMap["最终"]!!].text(),
                    scoreEntryChildren[nameMap["绩点"]!!].text().toDoubleOrNull() ?: 0.0,

                    scoreEntryChildren[nameMap["平时成绩"]!!].text().toDoubleOrNull() ?: -1.0,
                    scoreEntryChildren[nameMap["期中成绩"]!!].text().toDoubleOrNull() ?: -1.0,
                    scoreEntryChildren[nameMap["期末成绩"]!!].text().toDoubleOrNull() ?: -1.0,
                    scoreEntryChildren[nameMap["实验成绩"]!!].text().toDoubleOrNull() ?: -1.0,
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
            courseList.filter { it.semesterName == semesterName && !(it.courseType.contains("素养")||it.score.contains("P")) }
                .forEach { course -> selectCourse(course) }
        } else {
            courseList.filter { it.semesterName == semesterName }
                .forEach { course -> unselectCourse(course) }
        }
    }

    fun changeAllState() {
        if (_totalSum.value?.totalCredit==0.0) {
            courseList.filter {!(it.courseType.contains("素养")||it.score.contains("P")) }
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
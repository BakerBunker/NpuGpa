package com.bakerbunker.npugpa.model

data class Course(
    //Course descriptions
    val semesterName: String,
    val courseName: String,
    val courseNum: String,
    val courseType: String,

    //Scores
    val credit: Double,
    val score: String,
    val point: Double,

    //Score details
    val usual:Double,
    val midterm:Double,
    val endterm:Double,
    val experiment:Double
)
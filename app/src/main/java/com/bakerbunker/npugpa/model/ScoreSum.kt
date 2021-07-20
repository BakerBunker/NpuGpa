package com.bakerbunker.npugpa.model

data class ScoreSum(val totalCredit:Double,val avgScore:Double,val avgGpa:Double){
    operator fun plus(course: Course):ScoreSum{
        val credit=course.credit
        val newTotalCredit=totalCredit+credit
        val score=when(course.score){
            "P"->60.0
            "NP"->0.0
            else->course.score.toDouble()
        }
        return ScoreSum(newTotalCredit,(totalCredit*avgScore+score*credit)/newTotalCredit,(totalCredit*avgGpa+course.point*credit)/newTotalCredit)
    }

    operator fun minus(course: Course):ScoreSum{
        val credit=course.credit
        val newTotalCredit=totalCredit-credit
        if(newTotalCredit==0.0){
            return ScoreSum(0.0,0.0,0.0)
        }
        val score=when(course.score){
            "P"->60.0
            "NP"->0.0
            else->course.score.toDouble()
        }
        return ScoreSum(newTotalCredit,(totalCredit*avgScore-score*credit)/newTotalCredit,(totalCredit*avgGpa-course.point*credit)/newTotalCredit)
    }
}
package com.bakerbunker.npugpa.util

fun<T> MutableSet<T>.addOrRemove(element:T){
    if (this.contains(element)){
        this.remove(element)
    }else{
        this.add(element)
    }
}
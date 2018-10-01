package com.darryncampbell.datawedgekotlin

import java.util.*

class ObservableObject private constructor() : Observable() {

    fun updateValue(data: Any) {
        synchronized(this) {
            setChanged()
            notifyObservers(data)
        }
    }

    companion object {
        val instance = ObservableObject()
    }
}
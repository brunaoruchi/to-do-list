package com.example.todolist

class ToDoModel {

    companion object Factory {
        fun createList(): ToDoModel = ToDoModel()
    }

    var UID: String? = null
    var itemData: String? = null
    var done: Boolean? = null
}
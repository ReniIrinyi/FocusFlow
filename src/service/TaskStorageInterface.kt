package service

import model.Task
import utils.Priority

interface TaskStorageInterface {
    fun loadTasks(): Pair<List<Task>, Int>
    fun saveTasks(tasks: List<Task>): Int
    fun updateTask(taskId: Int, updatedData: Task): Int
    fun addTask(newTask: Task): Int
    fun deleteTaskById(taskId: Int): Int
    fun updateTaskPriority(taskId: Int, newPriority: Enum<Priority>): Int
    fun updateTaskStatus(taskId: Int, newStatus: Int?): Int
}

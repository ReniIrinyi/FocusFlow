package view.admin

import javafx.geometry.Insets
import javafx.scene.control.*
import javafx.scene.layout.*
import model.Task
import model.User
import controller.GenericController

class TaskOverview(
    private val taskController: GenericController<Task>,
    private val userController: GenericController<User>
) : BorderPane() {

    private lateinit var taskTreeView: TreeView<String>
    private lateinit var userDropdown: ComboBox<Pair<Int, String>>
    private lateinit var taskDetailsPane: VBox
    private val users = userController.createRequest("GET", null, null, null, "all").first as List<User>
    private val tasks = taskController.createRequest("GET", null, null, null, "all").first as List<Task>

    fun createView(): BorderPane {
        val header = createHeader()
        taskDetailsPane = VBox().apply {
            children.add(Label("Selektiere eine Aufgabe, um Details anzuzeigen oder zu bearbeiten."))
        }

        taskTreeView = TreeView<String>().apply {
            prefWidth = 300.0
            VBox.setVgrow(this, Priority.ALWAYS)
            isShowRoot = false
        }
        updateTreeView(null)

        taskTreeView.selectionModel.selectedItemProperty().addListener { _, _, selectedItem ->
            selectedItem?.let {
                if (!selectedItem.value.contains(":")) return@addListener
                val taskId = selectedItem.value.substringBefore(":").trim().toIntOrNull()
                if (taskId != null) {
                    val response = taskController.createRequest("GET", taskId, null, null, "byId").first
                    if (response is Task) {
                        val task = response
                        println(task)
                        val user = userController.createRequest("GET", null, task.userId, null, "byId").first as User
                        val taskManager = TaskManager(taskController, userController, task, user).createView()
                        taskDetailsPane.children.setAll(taskManager)
                    } else {
                        taskDetailsPane.children.setAll(Label("Fehler beim Laden der Aufgabe."))
                    }
                }
            }
        }


        val leftPane = VBox( header, taskTreeView).apply {
            padding = Insets(0.0)
        }

        val taskContent = VBox( taskDetailsPane).apply {
            padding = Insets(0.0)
            style = "-fx-background-color: #f4f4f4;"
        }

        this.left = leftPane
        this.center = taskContent

        selectAdminUser()

        return this
    }

    private fun selectAdminUser() {
        val adminUser = users.find { it.role == 1 }
        if (adminUser != null) {
            userDropdown.selectionModel.select(adminUser.id to adminUser.name)
            updateTreeView(adminUser.id)
            val taskManager = TaskManager(taskController, userController, null, adminUser)
            taskDetailsPane.children.setAll(taskManager.createView())
        }
    }

    private fun createHeader(): HBox {
        userDropdown = ComboBox<Pair<Int, String>>().apply {
            promptText = "Benutzer auswählen"
            items.addAll(listOf(0 to "Alle Benutzer")+ users.map { it.id to it.name })
            prefWidth = 200.0
            setOnAction {
                updateTreeView(value?.first)
            }

        }



        val addButton = Button("Neue Aufgabe hinzufügen").apply {
            setOnAction {
                val selectedUser = userDropdown.value
                if (selectedUser != null) {
                    val userId = selectedUser.first
                    println(userId)
                    println(selectedUser)
                    val user = users.find { it.id == userId }
                    val taskManager = TaskManager(taskController, userController, null, user as User)
                    val taskView = taskManager.createView()

                    if (center == null || center !is VBox) {
                        center = VBox()
                    }

                    (center as VBox).children.setAll(taskView)
                } else {
                    Alert(Alert.AlertType.WARNING, "Bitte einen Benutzer auswählen!").showAndWait()
                }
            }
        }

        return HBox(10.0, userDropdown, addButton).apply {
            padding = Insets(10.0)
            style = "-fx-background-color: #E0E0E0; -fx-padding: 10;"
        }
    }

    private fun updateTreeView(selectedUserId: Int?) {
        val rootItem = TreeItem("Benutzer")
        val filteredUsers = if (selectedUserId == null || selectedUserId == 0) users else users.filter { it.id == selectedUserId }

        filteredUsers.forEach { user ->
            val userTasks = tasks.filter { it.userId == user.id }
            val userItem = TreeItem<String>("${user.name} (${userTasks.size})")

            userTasks.forEach { task ->
                userItem.children.add(TreeItem("${task.id}: ${task.title}"))
            }

            rootItem.children.add(userItem)
        }

        taskTreeView.root = rootItem
    }
}

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
    private val users = userController.read(null, null, null, "all").first as List<User>
    private val tasks = taskController.read( null, null, null, "all").first as List<Task>

    fun createView(): BorderPane {
        val header = createHeader()

        taskDetailsPane = VBox().apply {
            styleClass.add("grid-element")
        }

        taskTreeView = TreeView<String>().apply {
            prefWidth = 400.0
            VBox.setVgrow(this, Priority.ALWAYS)
            isShowRoot = false
        }
        updateTreeView(null)

        taskTreeView.selectionModel.selectedItemProperty().addListener { _, _, selectedItem ->
            selectedItem?.let {
                if (!selectedItem.value.contains(":")) return@addListener
                val taskId = selectedItem.value.substringBefore(":").trim().toIntOrNull()
                if (taskId != null) {
                    val response = taskController.read( taskId, null, null, "byId").first
                    if (response is Task) {
                        val task = response
                        val user = userController.read(null, task.userId, null, "byId").first as User
                        val taskManager = TaskManager(taskController, task, user).createView()
                        taskDetailsPane.children.setAll(taskManager)
                    } else {
                        taskDetailsPane.children.setAll(Label("Fehler beim Laden der Aufgabe."))
                    }
                }
            }
        }
        val list = VBox(header, taskTreeView).apply {
            styleClass.add("grid-element")
            VBox.setVgrow(this, Priority.ALWAYS)
        }

        val leftPane = VBox(list).apply {
            styleClass.add("task-content")
            padding = Insets(20.0, 20.0, 20.0, 20.0)
            VBox.setVgrow(this, Priority.ALWAYS)
        }

        val taskContent = VBox(taskDetailsPane).apply {
            padding = Insets(20.0,20.0,20.0,0.0)
            styleClass.add("task-content")
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
            val taskManager = TaskManager(taskController, null, adminUser)
            taskDetailsPane.children.setAll(taskManager.createView())
        }
    }

    private fun createHeader(): HBox {
        userDropdown = ComboBox<Pair<Int, String>>().apply {
            promptText = "Benutzer auswählen"
            items.addAll(listOf(0 to "Alle Benutzer") + users.map { it.id to it.name })
            prefWidth = 250.0
            styleClass.add("dropdown")
            setOnAction {
                updateTreeView(value?.first)
            }
        }

        val addButton = Button("Neue Aufgabe hinzufügen").apply {
            styleClass.add("custom-button")
            setOnAction {
                val selectedUser = userDropdown.value
                if (selectedUser != null) {
                    val userId = selectedUser.first
                    val user = users.find { it.id == userId }
                    val taskManager = TaskManager(taskController, null, user as User)
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

        return HBox(15.0, userDropdown, addButton).apply {
            padding = Insets(15.0)
            styleClass.add("toolbar-header")
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

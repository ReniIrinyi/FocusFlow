package view.admin

import javafx.geometry.Insets
import javafx.scene.control.*
import javafx.scene.layout.*
import model.Task
import model.User
import controller.GenericController
import utils.Constants

class TaskManagerMenu(
    private val taskController: GenericController<Task>,
    private val userController: GenericController<User>
) : BorderPane() {
    private lateinit var taskTreeView: TreeView<String>
    private lateinit var userDropdown: ComboBox<Pair<Int, String>>
    private lateinit var taskEditorContainer: VBox
    private val users = userController.read(null, null, null, "all").first as List<User>
    private var tasks = taskController.read( null, null, null, "all").first as List<Task>

    fun createView(): BorderPane {
        val header = createHeader()

        taskEditorContainer = VBox().apply {
            styleClass.add("grid-element")
        }

        taskTreeView = TreeView<String>().apply {
            prefWidth = 400.0
            VBox.setVgrow(this, Priority.ALWAYS)
            isShowRoot = false
        }

        taskTreeView.selectionModel.selectedItemProperty().addListener { _, _, selectedItem ->
            selectedItem?.let {
                if (!selectedItem.value.contains(":")) return@addListener
                val taskId = selectedItem.value.substringBefore(":").trim().toIntOrNull()
                if (taskId != null) {
                    val response = taskController.read( taskId, null, null, "byId").first
                    if (response is Task) {
                        val user = userController.read(response.userId, response.userId, null, "byId")
                        if(user.second != Constants.STATUS_NOT_FOUND){
                            val user = user.first as User
                            updateTaskEditor(user.id to user.name,response as Task)
                        } else {
                            Alert(Alert.AlertType.WARNING, "Da ist etwas schiefgegangen. Bitte einen gültigen Benutzer auswählen!").showAndWait()
                        }
                    } else {
                        taskEditorContainer.children.setAll(Label("Fehler beim Laden der Aufgabe."))
                    }
                }
            }
        }

        val list = VBox(header, taskTreeView).apply {
            styleClass.add("grid-element")
            VBox.setVgrow(this, Priority.ALWAYS)
        }

        val taskTreePane = VBox(list).apply {
            styleClass.add("task-content")
            padding = Insets(20.0, 20.0, 20.0, 20.0)
            VBox.setVgrow(this, Priority.ALWAYS)
        }

        val taskEditorPane = VBox(taskEditorContainer).apply {
            padding = Insets(20.0,20.0,20.0,0.0)
            styleClass.add("task-content")
        }

        this.left = taskTreePane
        this.center = taskEditorPane

        //admin wird selected damit die taskEditor initialisiert wird
        selectAdmin()

        return this
    }


    private fun createHeader(): HBox {
        userDropdown = ComboBox<Pair<Int, String>>().apply {
            promptText = "Benutzer auswählen"
            items.addAll(listOf(0 to "Alle Benutzer") + users.map { it.id to it.name })
            prefWidth = 250.0
            styleClass.add("dropdown")
            setOnAction {
                updateTaskTree(value?.first)
                if(value.first != 0){
                    updateTaskEditor(value, null)
                }
            }
        }

        val addButton = Button("Neue Aufgabe hinzufügen").apply {
            styleClass.add("custom-button")
            setOnAction {
                val selectedUser = userDropdown.value
                if (selectedUser != null) {
                    updateTaskEditor(selectedUser, null)
                    updateTaskTree(selectedUser.first)
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

    private fun updateTaskEditor(selectedUser:Pair<Int,String>, task:Task?){
        val userId = selectedUser.first
        val user = users.find { it.id == userId }
        val taskEditor = TaskEditor(taskController, task, user as User){
            tasks = taskController.read( null, null, null, "all").first as List<Task>
            updateTaskTree(selectedUser.first)
        }
        val taskView = taskEditor.createView()

        if (center == null || center !is VBox) {
            center = VBox()
        }

        (center as VBox).children.setAll(taskView)
    }

    private fun updateTaskTree(selectedUserId: Int?) {

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


    private fun selectAdmin() {
        val adminUser = users.find { it.role == 1 }
        if (adminUser != null) {
            userDropdown.selectionModel.select(adminUser.id to adminUser.name)
            updateTaskTree(adminUser.id)
            val taskEditor = TaskEditor(taskController, null, adminUser){
                tasks = taskController.read( null, null, null, "all").first as List<Task>
                updateTaskTree(adminUser.id)
            }
            taskEditorContainer.children.setAll(taskEditor.createView())
        }
    }
}

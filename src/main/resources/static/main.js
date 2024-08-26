document.addEventListener('DOMContentLoaded', function() {

    // ----------------------------------- Move task -----------------------------------

    const moveButtons = document.querySelectorAll('.moveTask');

    moveButtons.forEach(button => {
        button.addEventListener('click', function() {
            const taskId = this.getAttribute('data-task-id');
            const direction = this.getAttribute('data-direction');
            moveTask(taskId, direction);
        });
    });

    function sortTasksByHeight() {
        document.querySelectorAll('.tasks').forEach(tasksContainer => {
            const tasks = Array.from(tasksContainer.children);
            tasks.sort((a, b) => {
                return parseInt(a.dataset.heightIndex) - parseInt(b.dataset.heightIndex);
            });
            tasks.forEach(task => tasksContainer.appendChild(task));
        });
    }

    sortTasksByHeight();


    function moveTask(taskId, direction) {
        fetch(`/api/task/${taskId}/move?direction=${direction}`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
        })
        .then(response => response.json())
        .then(data => {
            window.location.reload();
        })
        .catch((error) => {
            console.error('Error:', error);
        });
    }


    // ----------------------------------- Modal -----------------------------------

    const modal = document.getElementById('taskModal');
    const closeBtn = document.getElementsByClassName('close')[0];
    const cancelBtn = document.getElementById('cancelButton');
    const deleteBtn = document.getElementById('deleteButton');
    const taskForm = document.getElementById('taskForm');
    const modalTitle = document.getElementById('modalTitle');
    const dayNameSpan = document.getElementById('dayName');

    // Existing task
    document.querySelectorAll('.taskContent').forEach(task => {
        task.addEventListener('click', function() {
            const taskId = this.getAttribute('data-task-id');
            openModal('edit', taskId);
        });
    });

    // New task
    document.querySelectorAll('.addTask').forEach(button => {
        button.addEventListener('click', function() {
            const day = this.getAttribute('data-day');
            openModal('create', null, day);
        });
    });

    closeBtn.onclick = closeModal;
    cancelBtn.onclick = closeModal;

    deleteBtn.onclick = function() {
        const formData = new FormData(taskForm);
        const taskData = Object.fromEntries(formData.entries());

        fetch(`/api/task/${taskData.id}`, {
            method: 'DELETE',
            headers: {
                'Content-Type': 'application/json',
            }
        })
        .then(response => {
            if (response.ok) {
                closeModal();
                location.reload();
            } else {
                console.error('Error deleting task');
            }
        });
    }

    taskForm.onsubmit = function(e) {
        e.preventDefault();
        const formData = new FormData(taskForm);
        const taskData = Object.fromEntries(formData.entries());

        if (!taskData.id) {
            taskData.day = document.getElementById('taskDay').value;
            taskData.id = 0;
        }

        fetch('/api/task', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify(taskData)
        })
        .then(response => {
            if (response.ok) {
                closeModal();
                location.reload();
            } else {
                console.error('Error saving task');
            }
        });
    };

    function openModal(mode, taskId = null, day = null) {
        modal.style.display = 'block';
        if (mode === 'edit') {
            modalTitle.textContent = 'Edit Task';
            deleteBtn.style.display = 'block';
            fetch(`/api/task/${taskId}`)
                .then(response => response.json())
                .then(task => {
                    document.getElementById('taskId').value = task.id;
                    document.getElementById('taskTitle').value = task.title;
                    document.getElementById('taskNote').value = task.note;
                    document.getElementById('taskDay').value = task.day;
                    document.querySelector(`input[name="type"][value="${task.type}"]`).checked = true;
                    dayNameSpan.textContent = task.day;
                });
        } else {
            modalTitle.textContent = 'Create New Task';
            deleteBtn.style.display = 'none';
            taskForm.reset();
            document.getElementById('taskId').value = '';
            document.getElementById('taskDay').value = day;
            dayNameSpan.textContent = day;
            document.querySelector('input[name="type"][value="IMPORTANT"]').checked = true;
        }
    }

    function closeModal() {
        modal.style.display = 'none';
        taskForm.reset();
    }
});

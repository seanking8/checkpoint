$(document).ready(function () {
    let backlog;
    let apiUrl = 'http://localhost:8080/backlog';
	const userRole = 'edit';
    const canEdit = userRole === 'edit';

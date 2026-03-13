const App = (function () {

    let _token    = null;
    let _role     = null;
    let _username = null;
    let _backlogDataTable = null;

    function _showAlert(selector, message, type) {
        const el = $(selector);
        el.removeClass('d-none alert-success alert-danger alert-warning')
          .addClass('alert-' + (type || 'danger'))
          .text(message);
    }

    function _hideAlert(selector) {
        $(selector).addClass('d-none').text('');
    }

    // Token storage
    function _storeToken(token, role, username) {
        _token    = token;
        _role     = role;
        _username = username;
        localStorage.setItem('cp_token',    token);
        localStorage.setItem('cp_role',     role);
        localStorage.setItem('cp_username', username);
        // Attach token to every future AJAX call automatically
        $.ajaxSetup({ headers: { 'Authorization': 'Bearer ' + token } });
    }

    function _clearToken() {
        _token = _role = _username = null;
        localStorage.removeItem('cp_token');
        localStorage.removeItem('cp_role');
        localStorage.removeItem('cp_username');
        $.ajaxSetup({ headers: { 'Authorization': '' } });
    }

    // View switching
    function _showAuthView() {
        $('#view-auth').removeClass('d-none').css('display', 'flex');
        $('#view-app').addClass('d-none');
    }

    function _showAppView() {
        $('#view-auth').addClass('d-none');
        $('#view-app').removeClass('d-none');
        $('#navUsername').text(_username);
        if (_role === 'ROLE_ADMIN') {
            $('#adminNav').removeClass('d-none');
        }
        _loadBacklog();
    }

    function _statusBadgeClass(status) {
        if (status === 'COMPLETED') return 'text-bg-success';
        if (status === 'IN_PROGRESS') return 'text-bg-primary';
        if (status === 'DROPPED') return 'text-bg-danger';
        return 'text-bg-secondary';
    }

    function _labelStatus(status) {
        return String(status || 'WANT_TO_PLAY').replaceAll('_', ' ');
    }

    function _escapeHtml(value) {
        return String(value ?? '')
            .replaceAll('&', '&amp;')
            .replaceAll('<', '&lt;')
            .replaceAll('>', '&gt;')
            .replaceAll('"', '&quot;')
            .replaceAll("'", '&#39;');
    }

    function _setBacklogCount(count) {
        $('#backlogCount').text(count + (count === 1 ? ' item' : ' items'));
    }

    function _statusOptions(currentStatus) {
        const statuses = ['WANT_TO_PLAY', 'IN_PROGRESS', 'COMPLETED', 'DROPPED'];
        return statuses.map(function (status) {
            const selected = status === currentStatus ? ' selected' : '';
            return '<option value="' + status + '"' + selected + '>' + _escapeHtml(_labelStatus(status)) + '</option>';
        }).join('');
    }

    function _destroyBacklogDataTable() {
        if ($.fn.DataTable && $.fn.dataTable.isDataTable('#backlogTable')) {
            $('#backlogTable').DataTable().destroy();
        }
        _backlogDataTable = null;
    }

    function _initBacklogDataTable() {
        if (!$.fn.DataTable) {
            return;
        }

        _backlogDataTable = $('#backlogTable').DataTable({
            pageLength: 10,
            lengthChange: false,
            order: [[0, 'asc']],
            autoWidth: false
        });
    }

    function _renderBacklogRows(items) {
        const rows = items.map(function (item) {
            const rowId = Number(item.id);
            const gameTitle = _escapeHtml(item.gameTitle || 'Untitled game');
            const platformName = _escapeHtml(item.platformName || 'Unknown platform');
            const status = item.status || 'WANT_TO_PLAY';
            return '<tr>' +
                '<td class="fw-semibold">' + gameTitle + '</td>' +
                '<td>' + platformName + '</td>' +
                '<td>' +
                    '<select class="form-select form-select-sm backlog-status" data-id="' + rowId + '">' +
                        _statusOptions(status) +
                    '</select>' +
                '</td>' +
                '<td>' +
                    '<button type="button" class="btn btn-sm btn-outline-danger backlog-remove" data-id="' + rowId + '">Remove</button>' +
                '</td>' +
                '</tr>';
        }).join('');

        $('#backlogTableBody').html(rows);
    }

    function _wireBacklogActions() {
        $('#backlogTable').on('change', '.backlog-status', function () {
            const selectEl = $(this);
            const backlogId = Number(selectEl.data('id'));
            const status = String(selectEl.val() || 'WANT_TO_PLAY');

            selectEl.prop('disabled', true);
            $.ajax({
                method: 'PUT',
                url: '/api/me/backlog/' + backlogId + '/status',
                contentType: 'application/json',
                data: JSON.stringify({ status: status }),
                success: function () {
                    _showAlert('#backlogAlert', 'Status updated.', 'success');
                },
                error: function () {
                    _showAlert('#backlogAlert', 'Could not update status. Refreshing backlog.', 'danger');
                    _loadBacklog();
                },
                complete: function () {
                    selectEl.prop('disabled', false);
                }
            });
        });

        $('#backlogTable').on('click', '.backlog-remove', function () {
            const buttonEl = $(this);
            const backlogId = Number(buttonEl.data('id'));

            if (!window.confirm('Remove this game from your backlog?')) {
                return;
            }

            buttonEl.prop('disabled', true);
            $.ajax({
                method: 'DELETE',
                url: '/api/me/backlog/' + backlogId,
                success: function () {
                    _showAlert('#backlogAlert', 'Backlog item removed.', 'success');
                    _loadBacklog();
                },
                error: function () {
                    buttonEl.prop('disabled', false);
                    _showAlert('#backlogAlert', 'Could not remove backlog item.', 'danger');
                }
            });
        });
    }

    function _loadBacklog() {
        _destroyBacklogDataTable();
        $('#backlogTableBody').empty();
        _hideAlert('#backlogAlert');
        $('#backlogLoading').removeClass('d-none');
        $('#backlogEmpty').addClass('d-none');
        $('#backlogTableWrap').addClass('d-none');
        _setBacklogCount(0);

        $.ajax({
            method: 'GET',
            url: '/api/me/backlog',
            success: function (data) {
                const items = Array.isArray(data) ? data : [];
                $('#backlogLoading').addClass('d-none');
                _setBacklogCount(items.length);

                if (items.length === 0) {
                    $('#backlogEmpty').removeClass('d-none');
                    return;
                }

                _renderBacklogRows(items);
                $('#backlogTableWrap').removeClass('d-none');
                _initBacklogDataTable();
            },
            error: function (xhr) {
                $('#backlogLoading').addClass('d-none');
                if (xhr.status === 401) {
                    logout();
                    _showAlert('#authAlert', 'Your session expired. Please log in again.', 'warning');
                    return;
                }
                _showAlert('#backlogAlert', 'Could not load your backlog right now. Please try again.', 'danger');
            }
        });
    }

    // Boot. runs once on page load
    function _boot() {
        _wireBacklogActions();

        const savedToken = localStorage.getItem('cp_token');
        if (savedToken) {
            // Token already exists — restore it and go straight to the app
            _storeToken(
                savedToken,
                localStorage.getItem('cp_role'),
                localStorage.getItem('cp_username')
            );
            _showAppView();
        } else {
            _showAuthView();
        }

        // Login form
        $('#loginForm').on('submit', function (e) {
            e.preventDefault();
            _hideAlert('#authAlert');

            $.ajax({
                method: 'POST',
                url: '/api/auth/login',
                contentType: 'application/json',
                data: JSON.stringify({
                    username: $('#loginUsername').val().trim(),
                    password: $('#loginPassword').val()
                }),
                success: function (data) {
                    _storeToken(data.token, data.role, data.username);
                    $('#loginForm')[0].reset();
                    _showAppView();
                },
                error: function (xhr) {
                    _showAlert('#authAlert',
                        xhr.status === 401 ? 'Invalid username or password.' : 'Login failed. Please try again.',
                        'danger');
                }
            });
        });

        // Register form
        $('#registerForm').on('submit', function (e) {
            e.preventDefault();
            _hideAlert('#authAlert');

            const password = $('#regPassword').val();
            const confirmPassword = $('#regConfirmPassword').val();

            // Client-side check before hitting the server
            if (password !== confirmPassword) {
                _showAlert('#authAlert', 'Passwords do not match.', 'danger');
                return;
            }

            $.ajax({
                method: 'POST',
                url: '/api/auth/register',
                contentType: 'application/json',
                data: JSON.stringify({
                    username: $('#regUsername').val().trim(),
                    password: password,
                    confirmPassword: confirmPassword
                }),
                success: function () {
                    _showAlert('#authAlert', 'Account created! You can now log in.', 'success');
                    showAuthTab('login');
                    $('#loginUsername').val($('#regUsername').val().trim());
                    $('#registerForm')[0].reset();
                },
                error: function (xhr) {
                    _showAlert('#authAlert',
                        xhr.status === 409 ? 'Username already taken.' : 'Registration failed. Please try again.',
                        'danger');
                }
            });
        });
    }

    // Auth tab toggle
    function showAuthTab(tab) {
        _hideAlert('#authAlert');
        if (tab === 'login') {
            $('#loginForm').removeClass('d-none');
            $('#registerForm').addClass('d-none');
            $('#loginTabBtn').addClass('active');
            $('#registerTabBtn').removeClass('active');
        } else {
            $('#loginForm').addClass('d-none');
            $('#registerForm').removeClass('d-none');
            $('#loginTabBtn').removeClass('active');
            $('#registerTabBtn').addClass('active');
        }
    }

    function logout() {
        _destroyBacklogDataTable();
        _clearToken();
        _showAuthView();
        showAuthTab('login');
    }

    // Init
    $(document).ready(_boot);

    // Public API
    return { showAuthTab, logout };

})();

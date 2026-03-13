const App = (function () {

    let _token    = null;
    let _role     = null;
    let _username = null;
    let _backlogDataTable = null;
    let _libraryDataTable = null;
    let _backlogLoadSeq = 0;
    let _libraryLoadSeq = 0;

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
        showView('backlog');
    }

    function _setActiveNavButton(viewName) {
        $('#navBacklogBtn').toggleClass('active', viewName === 'backlog');
        $('#navLibraryBtn').toggleClass('active', viewName === 'library');
    }

    function _showBacklogSection() {
        _setActiveNavButton('backlog');
        $('#backlogSection').removeClass('d-none');
        $('#librarySection').addClass('d-none');
    }

    function _showLibrarySection() {
        _setActiveNavButton('library');
        $('#backlogSection').addClass('d-none');
        $('#librarySection').removeClass('d-none');
    }

    function _setLibraryCount(count) {
        $('#libraryCount').text(count + (count === 1 ? ' game' : ' games'));
    }

    function _destroyLibraryDataTable() {
        if ($.fn.DataTable && $.fn.dataTable.isDataTable('#libraryTable')) {
            $('#libraryTable').DataTable().destroy();
        }
        _libraryDataTable = null;
    }

    function _initLibraryDataTable() {
        if (!$.fn.DataTable) {
            return;
        }

        if ($.fn.dataTable.isDataTable('#libraryTable')) {
            $('#libraryTable').DataTable().destroy();
        }

        _libraryDataTable = $('#libraryTable').DataTable({
            pageLength: 10,
            lengthChange: false,
            order: [[0, 'asc']],
            autoWidth: false,
            destroy: true
        });
    }

    function _renderLibraryRows(items) {
        const rows = items.map(function (item) {
            const title = _escapeHtml(item.title || 'Untitled game');
            const platforms = Array.isArray(item.platforms) && item.platforms.length > 0
                ? _escapeHtml(item.platforms.join(', '))
                : 'N/A';
            const year = Number(item.releaseYear) || '-';

            return '<tr>' +
                '<td class="fw-semibold">' + title + '</td>' +
                '<td>' + platforms + '</td>' +
                '<td>' + year + '</td>' +
                '</tr>';
        }).join('');

        $('#libraryTableBody').html(rows);
    }

    function _loadLibrary() {
        const loadSeq = ++_libraryLoadSeq;
        _destroyLibraryDataTable();
        $('#libraryTableBody').empty();
        _hideAlert('#libraryAlert');
        $('#libraryLoading').removeClass('d-none');
        $('#libraryEmpty').addClass('d-none');
        $('#libraryTableWrap').addClass('d-none');
        _setLibraryCount(0);

        $.ajax({
            method: 'GET',
            url: '/api/games',
            success: function (data) {
                if (loadSeq !== _libraryLoadSeq) {
                    return;
                }
                const items = Array.isArray(data) ? data : [];
                $('#libraryLoading').addClass('d-none');
                _setLibraryCount(items.length);

                if (items.length === 0) {
                    $('#libraryEmpty').removeClass('d-none');
                    return;
                }

                _renderLibraryRows(items);
                $('#libraryTableWrap').removeClass('d-none');
                _initLibraryDataTable();
            },
            error: function (xhr) {
                if (loadSeq !== _libraryLoadSeq) {
                    return;
                }
                $('#libraryLoading').addClass('d-none');
                if (xhr.status === 401) {
                    logout();
                    _showAlert('#authAlert', 'Your session expired. Please log in again.', 'warning');
                    return;
                }
                _showAlert('#libraryAlert', 'Could not load the global library right now. Please try again.', 'danger');
            }
        });
    }

    function showView(viewName) {
        if (viewName === 'library') {
            _showLibrarySection();
            _loadLibrary();
            return;
        }
        _showBacklogSection();
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

        if ($.fn.dataTable.isDataTable('#backlogTable')) {
            $('#backlogTable').DataTable().destroy();
        }

        _backlogDataTable = $('#backlogTable').DataTable({
            pageLength: 10,
            lengthChange: false,
            order: [[0, 'asc']],
            autoWidth: false,
            destroy: true
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
        const loadSeq = ++_backlogLoadSeq;
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
                if (loadSeq !== _backlogLoadSeq) {
                    return;
                }
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
                if (loadSeq !== _backlogLoadSeq) {
                    return;
                }
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
        _destroyLibraryDataTable();
        _clearToken();
        _showAuthView();
        showAuthTab('login');
    }

    // Init
    $(document).ready(_boot);

    // Public API
    return { showAuthTab, showView, logout };

})();

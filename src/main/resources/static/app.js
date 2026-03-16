const App = (function () {
    'use strict';

    let _token = null;
    let _role = null;
    let _username = null;
    let _backlogDataTable = null;
    let _libraryDataTable = null;
    let _backlogLoadSeq = 0;
    let _backlogItems = [];
    let _backlogChart = null;

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
        _token = token;
        _role = role;
        _username = username;
        localStorage.setItem('cp_token', token);
        localStorage.setItem('cp_role', role);
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
        if (_isAdmin()) {
            $('#adminNav').removeClass('d-none');
            $('#addGameBtn').removeClass('d-none');
        } else {
            $('#adminNav').addClass('d-none');
            $('#addGameBtn').addClass('d-none');
        }
        showView('backlog');
    }

    function _isAdmin() {
        return _role === 'ADMIN' || _role === 'ROLE_ADMIN';
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

    function _setLibraryPlatformOptions(items) {
        const $select = $('#libraryPlatformFilter');
        $select.empty();

        const platforms = [];
        items.forEach(function (item) {
            (item.platforms || []).forEach(function (platform) {
                const platformName = platform && platform.name ? String(platform.name) : '';
                if (platformName && !platforms.includes(platformName)) {
                    platforms.push(platformName);
                }
            });
        });

        let html = '<option value="All Platforms" selected>All Platforms</option>';
        platforms.sort().forEach(function (platform) {
            html += '<option value="' + platform + '">' + platform + '</option>';
        });

        $select.append(html);
        $select.prop('disabled', platforms.length === 0);
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
            const rowId = Number(item.id);
            const title = _escapeHtml(item.title || 'Untitled game');
            const rawTitle = String(item.title || 'Untitled game');
            const platforms = Array.isArray(item.platforms) && item.platforms.length > 0
                ? _escapeHtml(item.platforms.map(function (platform) {
                    return platform && platform.name ? platform.name : 'Unknown platform';
                }).join(', '))
                : 'N/A';
            const platformOptions = Array.isArray(item.platforms) && item.platforms.length > 0
                ? item.platforms.map(function (platformOption) {
                    const platformId = Number(platformOption.id);
                    const safePlatform = _escapeHtml(platformOption.name || 'Unknown platform');
                    return '<button type="button" class="dropdown-item backlog-add" data-id="' + rowId + '" data-platform-id="' + platformId + '">' + safePlatform + '</button>';
                }).join('')
                : '<span class="dropdown-item-text text-muted">No platforms available</span>';
            const year = Number(item.releaseYear) || '-';
            const rawYear = Number(item.releaseYear) || 0;
            const rawCoverArtUrl = String(item.coverArtUrl || '');
            const menuId = 'library-dropdown-' + rowId;
            const backlogAction = '<div class="dropdown d-inline-block me-2">' +
                '<button class="btn btn-secondary dropdown-toggle" type="button" id="' + menuId + '" data-bs-toggle="dropdown" aria-expanded="false">Add to Backlog</button>' +
                '<div class="dropdown-menu" aria-labelledby="' + menuId + '">' + platformOptions + '</div>' +
              '</div>';
            const adminActions = _isAdmin()
                ? '<div class="btn-group btn-group-sm" role="group" aria-label="Admin game actions">' +
                    '<button type="button" class="btn btn-outline-primary library-edit" data-id="' + rowId + '" data-title="' + _escapeHtml(rawTitle) + '" data-year="' + rawYear + '" data-cover="' + _escapeHtml(rawCoverArtUrl) + '">Edit</button>' +
                    '<button type="button" class="btn btn-outline-danger library-delete" data-id="' + rowId + '">Delete</button>' +
                  '</div>'
                : '';
            const actionCell = backlogAction + adminActions;

            const html = `
                            <tr>
                                <td class="fw-semibold">${title}</td>
                                <td>${platforms}</td>
                                <td>${year}</td>
                                <td>
                                    ${actionCell}
                                </td>
                            </tr>
                          `;

            return html;
        }).join('');

        $('#libraryTableBody').html(rows);
    }

    function _loadLibrary() {
        _destroyLibraryDataTable();
        $('#libraryTableBody').empty();
        $('#libraryPlatformFilter')
            .html('<option value="All Platforms" selected>All Platforms</option>')
            .prop('disabled', true);
        _hideAlert('#libraryAlert');
        $('#libraryLoading').removeClass('d-none');
        $('#libraryEmpty').addClass('d-none');
        $('#libraryTableWrap').addClass('d-none');
        _setLibraryCount(0);

        $.ajax({
            method: 'GET',
            url: '/api/games',
            success: function (data) {
                const items = Array.isArray(data) ? data : [];
                $('#libraryLoading').addClass('d-none');
                _setLibraryCount(items.length);

                if (items.length === 0) {
                    $('#libraryEmpty').removeClass('d-none');
                    return;
                }

                _renderLibraryRows(items);
                _setLibraryPlatformOptions(items);
                $('#libraryTableWrap').removeClass('d-none');
                _initLibraryDataTable();
            },
            error: function (xhr) {
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

    function _destroyBacklogChart() {
        if (_backlogChart) {
            _backlogChart.destroy();
            _backlogChart = null;
        }
    }

    function _backlogStatusCounts(items) {
        const counts = {
            COMPLETED: 0,
            WANT_TO_PLAY: 0,
            IN_PROGRESS: 0,
            DROPPED: 0
        };

        (items || []).forEach(function (item) {
            const status = String(item && item.status ? item.status : 'WANT_TO_PLAY');
            if (Object.prototype.hasOwnProperty.call(counts, status)) {
                counts[status] += 1;
            }
        });

        return counts;
    }

    function _renderBacklogAnalytics() {
        if ($('#backlogAnalyticsCard').hasClass('d-none')) {
            return;
        }

        const total = _backlogItems.length;
        const canvas = document.getElementById('backlogAnalyticsChart');
        const emptyEl = $('#backlogAnalyticsEmpty');
        const chartWrapEl = $('#backlogAnalyticsChartWrap');

        if (!canvas || typeof Chart === 'undefined') {
            chartWrapEl.addClass('d-none');
            emptyEl.removeClass('d-none').text('Analytics chart is unavailable right now.');
            $('#backlogAnalyticsSummary').text('');
            _destroyBacklogChart();
            return;
        }

        if (total === 0) {
            chartWrapEl.addClass('d-none');
            emptyEl.removeClass('d-none').text('No backlog data to visualize yet.');
            $('#backlogAnalyticsSummary').text('');
            _destroyBacklogChart();
            return;
        }

        const counts = _backlogStatusCounts(_backlogItems);
        const order = ['COMPLETED', 'WANT_TO_PLAY', 'IN_PROGRESS', 'DROPPED'];
        const labels = ['Completed', 'Want to Play', 'In Progress', 'Dropped'];
        const values = order.map(function (status) { return counts[status]; });
        const completedPct = Math.round((counts.COMPLETED / total) * 100);

        $('#backlogAnalyticsSummary').text('Completed: ' + counts.COMPLETED + ' / ' + total + ' (' + completedPct + '%)');
        emptyEl.addClass('d-none').text('');
        chartWrapEl.removeClass('d-none');

        _destroyBacklogChart();
        _backlogChart = new Chart(canvas, {
            type: 'pie',
            data: {
                labels: labels,
                datasets: [{
                    data: values,
                    backgroundColor: ['#198754', '#6c757d', '#0d6efd', '#dc3545']
                }]
            },
            options: {
                responsive: true,
                plugins: {
                    legend: {
                        position: 'bottom'
                    }
                }
            }
        });
    }

    function _setBacklogAnalyticsVisible(isVisible) {
        $('#backlogAnalyticsCard').toggleClass('d-none', !isVisible);
        $('#viewAnalyticsBtn').text(isVisible ? 'Hide Analytics' : 'View Analytics');

        if (isVisible) {
            _renderBacklogAnalytics();
        } else {
            _destroyBacklogChart();
        }
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
                    _backlogItems = _backlogItems.map(function (item) {
                        if (Number(item.id) === backlogId) {
                            return Object.assign({}, item, { status: status });
                        }
                        return item;
                    });
                    _renderBacklogAnalytics();
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

    function _wireBacklogAnalytics() {
        $('#viewAnalyticsBtn').on('click', function () {
            const currentlyVisible = !$('#backlogAnalyticsCard').hasClass('d-none');
            _setBacklogAnalyticsVisible(!currentlyVisible);
        });
    }

    function _wireLibraryFilter() {
        $('#libraryPlatformFilter').on('change', function () {
            if (!_libraryDataTable) {
                return;
            }
            const selectedPlatform = String($('#libraryPlatformFilter option:selected').val() || 'All Platforms');
            if (selectedPlatform === 'All Platforms') {
                _libraryDataTable.column(1).search('').draw();
            } else {
                _libraryDataTable.column(1).search(selectedPlatform).draw();
            }
        });
    }

    function _wireAddToBacklog() {
        $(document).on('click', '.backlog-add', function () {
            const gameId = Number($(this).data('id'));
            const platformId = Number($(this).data('platform-id'));

            if (!gameId || !platformId) {
                _showAlert('#libraryAlert', 'Could not add to backlog: missing game or platform.', 'danger');
                return;
            }

            _findById(gameId, platformId);
        });
    }

    function _setAddGamePlatformOptions(platforms) {
        const container = $('#addGamePlatforms');
        if (!platforms.length) {
            container.html('<div class="text-muted small">No platforms available.</div>');
            $('#addGameSubmitBtn').prop('disabled', true);
            return;
        }

        let html = '';
        platforms.forEach(function (platform) {
            const platformId = Number(platform.id);
            if (!platformId) {
                return;
            }
            const platformName = _escapeHtml(platform.name || 'Unknown platform');
            const checkboxId = 'add-game-platform-' + platformId;
            html += '<div class="form-check">' +
                '<input class="form-check-input add-game-platform" type="checkbox" value="' + platformId + '" id="' + checkboxId + '">' +
                '<label class="form-check-label" for="' + checkboxId + '">' + platformName + '</label>' +
                '</div>';
        });

        container.html(html || '<div class="text-muted small">No platforms available.</div>');
        $('#addGameSubmitBtn').prop('disabled', html.length === 0);
    }

    function _openAddGameModal() {
        const modalEl = document.getElementById('addGameModal');
        const modal = bootstrap.Modal.getOrCreateInstance(modalEl);

        $('#addGameForm')[0].reset();
        _hideAlert('#addGameModalAlert');
        $('#addGamePlatforms').html('<div class="text-muted small">Loading platforms...</div>');
        $('#addGameSubmitBtn').prop('disabled', true);
        modal.show();

        $.ajax({
            method: 'GET',
            url: '/api/platforms',
            success: function (data) {
                const platforms = Array.isArray(data) ? data : [];
                _setAddGamePlatformOptions(platforms);
            },
            error: function () {
                _showAlert('#addGameModalAlert', 'Could not load platform options.', 'danger');
                _setAddGamePlatformOptions([]);
            }
        });
    }

    function _submitAddGame() {
        const title = String($('#addGameTitle').val() || '').trim();
        const releaseYear = Number(String($('#addGameYear').val() || '').trim());
        const platformIds = $('.add-game-platform:checked').map(function () {
            return Number($(this).val());
        }).get().filter(function (id) {
            return Number.isInteger(id) && id > 0;
        });

        if (!title) {
            _showAlert('#addGameModalAlert', 'Game name is required.', 'warning');
            return;
        }

        if (!Number.isInteger(releaseYear) || releaseYear < 0) {
            _showAlert('#addGameModalAlert', 'Release year must be a valid positive number.', 'warning');
            return;
        }

        if (platformIds.length === 0) {
            _showAlert('#addGameModalAlert', 'Select at least one platform.', 'warning');
            return;
        }

        $('#addGameSubmitBtn').prop('disabled', true);
        $.ajax({
            method: 'POST',
            url: '/api/games',
            contentType: 'application/json',
            data: JSON.stringify({
                title: title,
                coverArtUrl: '',
                releaseYear: releaseYear,
                platformIds: platformIds
            }),
            success: function () {
                bootstrap.Modal.getOrCreateInstance(document.getElementById('addGameModal')).hide();
                _showAlert('#libraryAlert', 'Game added successfully.', 'success');
                _loadLibrary();
            },
            error: function (xhr) {
                $('#addGameSubmitBtn').prop('disabled', false);
                if (xhr.status === 409) {
                    _showAlert('#addGameModalAlert', 'A game with that title already exists.', 'warning');
                    return;
                }
                if (xhr.status === 400) {
                    _showAlert('#addGameModalAlert', 'Invalid game details. Check title, year, and platform selection.', 'warning');
                    return;
                }
                _showAlert('#addGameModalAlert', 'Could not add game right now.', 'danger');
            }
        });
    }

    function _wireAddGameModal() {
        $('#addGameBtn').on('click', function () {
            if (!_isAdmin()) {
                return;
            }
            _openAddGameModal();
        });

        $('#addGameForm').on('submit', function (e) {
            e.preventDefault();
            _hideAlert('#addGameModalAlert');
            _submitAddGame();
        });

        $('#addGameModal').on('hidden.bs.modal', function () {
            _hideAlert('#addGameModalAlert');
            $('#addGameForm')[0].reset();
            $('#addGameSubmitBtn').prop('disabled', false);
        });
    }

    function _wireAdminLibraryActions() {
        $(document).on('click', '.library-edit', function () {
            if (!_isAdmin()) {
                return;
            }

            const buttonEl = $(this);
            const gameId = Number(buttonEl.data('id'));
            const currentTitle = String(buttonEl.data('title') || '');
            const currentYear = Number(buttonEl.data('year')) || 0;
            const currentCover = String(buttonEl.data('cover') || '');

            const newTitleInput = window.prompt('Edit game title:', currentTitle);
            if (newTitleInput === null) {
                return;
            }
            const newTitle = String(newTitleInput).trim();
            if (!newTitle) {
                _showAlert('#libraryAlert', 'Title cannot be empty.', 'warning');
                return;
            }

            const newYearInput = window.prompt('Edit release year:', String(currentYear));
            if (newYearInput === null) {
                return;
            }
            const newYear = Number(String(newYearInput).trim());
            if (!Number.isInteger(newYear) || newYear < 0) {
                _showAlert('#libraryAlert', 'Release year must be a valid positive number.', 'warning');
                return;
            }

            buttonEl.prop('disabled', true);
            $.ajax({
                method: 'PUT',
                url: '/api/games/' + gameId,
                contentType: 'application/json',
                data: JSON.stringify({
                    title: newTitle,
                    coverArtUrl: currentCover,
                    releaseYear: newYear
                }),
                success: function () {
                    _showAlert('#libraryAlert', 'Game updated.', 'success');
                    _loadLibrary();
                },
                error: function (xhr) {
                    buttonEl.prop('disabled', false);
                    if (xhr.status === 409) {
                        _showAlert('#libraryAlert', 'A game with that title already exists.', 'warning');
                        return;
                    }
                    _showAlert('#libraryAlert', 'Could not update game.', 'danger');
                }
            });
        });

        $(document).on('click', '.library-delete', function () {
            if (!_isAdmin()) {
                return;
            }

            const buttonEl = $(this);
            const gameId = Number(buttonEl.data('id'));

            if (!window.confirm('Delete this game from the global library?')) {
                return;
            }

            buttonEl.prop('disabled', true);
            $.ajax({
                method: 'DELETE',
                url: '/api/games/' + gameId,
                success: function () {
                    _showAlert('#libraryAlert', 'Game deleted.', 'success');
                    _loadLibrary();
                },
                error: function () {
                    buttonEl.prop('disabled', false);
                    _showAlert('#libraryAlert', 'Could not delete game.', 'danger');
                }
            });
        });
    }

    const _findById = (gameId, platformId) => {
        $.ajax({
            method: 'POST',
            url: '/api/me/backlog',
            contentType: 'application/json',
            data: JSON.stringify({ gameId: gameId, platformId: platformId }),
            success: function () {
                _showAlert('#libraryAlert', 'Added to your backlog!', 'success');
            },
            error: function (xhr) {
                if (xhr.status === 409) {
                    _showAlert('#libraryAlert', 'That game/platform is already in your backlog.', 'warning');
                    return;
                }
                if (xhr.status === 400) {
                    _showAlert('#libraryAlert', 'Invalid game/platform selection.', 'danger');
                    return;
                }
                _showAlert('#libraryAlert', 'Could not add to backlog.', 'danger');
            }
        });
    }

    function _loadBacklog() {
        const loadSeq = ++_backlogLoadSeq;
        _destroyBacklogDataTable();
        _backlogItems = [];
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
                _backlogItems = items;
                $('#backlogLoading').addClass('d-none');
                _setBacklogCount(items.length);

                if (items.length === 0) {
                    $('#backlogEmpty').removeClass('d-none');
                    _renderBacklogAnalytics();
                    return;
                }

                _renderBacklogRows(items);
                $('#backlogTableWrap').removeClass('d-none');
                _initBacklogDataTable();
                _renderBacklogAnalytics();
            },
            error: function (xhr) {
                if (loadSeq !== _backlogLoadSeq) {
                    return;
                }
                _backlogItems = [];
                _renderBacklogAnalytics();
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
        _wireBacklogAnalytics();
        _wireLibraryFilter();
        _wireAddToBacklog();
        _wireAdminLibraryActions();
        _wireAddGameModal();

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
        _destroyBacklogChart();
        _backlogItems = [];
        _setBacklogAnalyticsVisible(false);
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

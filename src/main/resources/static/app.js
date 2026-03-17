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
    let _backlogPlatformChart = null;
    let _libraryGameById = {};
    let _editingGameId = null;
    let _editingGameCoverArtUrl = '';
    let _confirmActionHandler = null;

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
        platforms.sort(function (a, b) {
            return a.localeCompare(b, undefined, { sensitivity: 'base' });
        }).forEach(function (platform) {
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
        _libraryGameById = {};
        const rows = items.map(function (item) {
            const rowId = Number(item.id);
            _libraryGameById[rowId] = item;
            const title = _escapeHtml(item.title || 'Untitled game');
            const rawTitle = String(item.title || 'Untitled game');
            const safeTitleAttr = _escapeHtml(rawTitle);
            const platforms = Array.isArray(item.platforms) && item.platforms.length > 0
                ? _escapeHtml(item.platforms.map(function (platform) {
                    return platform?.name || 'Unknown platform';
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
                '<button class="btn btn-sm btn-secondary dropdown-toggle library-backlog-btn" type="button" id="' + menuId + '" data-bs-toggle="dropdown" aria-expanded="false">' +
                    '<i class="bi bi-plus-lg me-1" aria-hidden="true"></i>Add to Backlog' +
                '</button>' +
                '<div class="dropdown-menu" aria-labelledby="' + menuId + '">' + platformOptions + '</div>' +
              '</div>';
            const adminActions = _isAdmin()
                ? '<div class="btn-group" role="group" aria-label="Admin game actions">' +
                    '<button type="button" class="btn btn-outline-primary library-edit icon-only-btn" data-id="' + rowId + '" data-title="' + _escapeHtml(rawTitle) + '" data-year="' + rawYear + '" data-cover="' + _escapeHtml(rawCoverArtUrl) + '" aria-label="Edit game" title="Edit game">' +
                        '<i class="bi bi-wrench" aria-hidden="true"></i>' +
                        '<span class="visually-hidden">Edit</span>' +
                    '</button>' +
                    '<button type="button" class="btn btn-outline-danger library-delete icon-only-btn" data-id="' + rowId + '" data-title="' + safeTitleAttr + '" aria-label="Delete game" title="Delete game">' +
                        '<i class="bi bi-x-lg" aria-hidden="true"></i>' +
                        '<span class="visually-hidden">Delete</span>' +
                    '</button>' +
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
        if (_backlogPlatformChart) {
            _backlogPlatformChart.destroy();
            _backlogPlatformChart = null;
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

    function _backlogPlatformCounts(items) {
        const counts = {};

        (items || []).forEach(function (item) {
            const name = String(item?.platformName || 'Unknown platform').trim() || 'Unknown platform';
            counts[name] = (counts[name] || 0) + 1;
        });

        return Object.keys(counts)
            .sort(function (a, b) {
                if (counts[b] !== counts[a]) {
                    return counts[b] - counts[a];
                }
                return a.localeCompare(b, undefined, { sensitivity: 'base' });
            })
            .map(function (name) {
                return { name: name, count: counts[name] };
            });
    }

    function _renderBacklogAnalytics() {
        if ($('#backlogAnalyticsPanel').hasClass('d-none')) {
            return;
        }

        const total = _backlogItems.length;
        const canvas = document.getElementById('backlogAnalyticsChart');
        const platformCanvas = document.getElementById('backlogPlatformChart');
        const emptyEl = $('#backlogAnalyticsEmpty');
        const chartWrapEl = $('#backlogAnalyticsChartWrap');
        const platformChartWrapEl = $('#backlogPlatformChartWrap');

        if (!canvas || !platformCanvas || typeof Chart === 'undefined') {
            chartWrapEl.addClass('d-none');
            platformChartWrapEl.addClass('d-none');
            emptyEl.removeClass('d-none').text('Analytics chart is unavailable right now.');
            $('#backlogAnalyticsSummary').text('');
            _destroyBacklogChart();
            return;
        }

        if (total === 0) {
            chartWrapEl.addClass('d-none');
            platformChartWrapEl.addClass('d-none');
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
        platformChartWrapEl.removeClass('d-none');

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
                maintainAspectRatio: false,
                plugins: {
                    legend: {
                        position: 'bottom'
                    }
                }
            }
        });

        const platformRows = _backlogPlatformCounts(_backlogItems);
        _backlogPlatformChart = new Chart(platformCanvas, {
            type: 'bar',
            data: {
                labels: platformRows.map(function (row) { return row.name; }),
                datasets: [{
                    label: 'Backlog Entries',
                    data: platformRows.map(function (row) { return row.count; }),
                    backgroundColor: '#0d6efd'
                }]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                scales: {
                    y: {
                        beginAtZero: true,
                        ticks: {
                            precision: 0
                        }
                    }
                },
                plugins: {
                    legend: {
                        display: false
                    }
                }
            }
        });
    }

    function _setBacklogAnalyticsVisible(isVisible) {
        $('#backlogAnalyticsPanel').toggleClass('d-none', !isVisible);
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
            const gameTitleAttr = _escapeHtml(String(item.gameTitle || 'Untitled game'));
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
                '<button type="button" class="btn btn-sm btn-outline-danger backlog-remove icon-only-btn" data-id="' + rowId + '" data-title="' + gameTitleAttr + '" aria-label="Remove from backlog" title="Remove from backlog">' +
                '<i class="bi bi-x-lg" aria-hidden="true"></i>' +
                '<span class="visually-hidden">Remove</span>' +
                '</button>' +
                '</td>' +
                '</tr>';
        }).join('');

        $('#backlogTableBody').html(rows);
    }

    function _setLocalBacklogItemStatus(backlogId, status) {
        _backlogItems = _backlogItems.map(function (item) {
            if (Number(item.id) === backlogId) {
                return Object.assign({}, item, { status: status });
            }
            return item;
        });
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
                    _setLocalBacklogItemStatus(backlogId, status);
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
            const gameTitle = String(buttonEl.data('title') || 'this game');
            _openConfirmActionModal({
                title: 'Remove Backlog Item',
                message: 'Are you sure you want to remove "' + gameTitle + '" from your backlog?',
                confirmLabel: 'Remove',
                confirmButtonClass: 'btn-danger',
                onConfirm: function () {
                    _deleteBacklogItem(backlogId);
                }
            });
        });
    }

    function _wireBacklogAnalytics() {
        $('#viewAnalyticsBtn').on('click', function () {
            const currentlyVisible = !$('#backlogAnalyticsPanel').hasClass('d-none');
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

    function _setGamePlatformOptions(containerSelector, checkboxClass, submitButtonSelector, platforms, selectedPlatformIds) {
        const container = $(containerSelector);
        const selectedIds = new Set((selectedPlatformIds || []).map(function (id) {
            return Number(id);
        }));

        if (!platforms.length) {
            container.html('<div class="text-muted small">No platforms available.</div>');
            $(submitButtonSelector).prop('disabled', true);
            return;
        }

        let html = '';
        platforms.forEach(function (platform) {
            const platformId = Number(platform.id);
            if (!platformId) {
                return;
            }
            const platformName = _escapeHtml(platform.name || 'Unknown platform');
            const checkboxId = checkboxClass + '-' + platformId;
            const checked = selectedIds.has(platformId) ? ' checked' : '';
            html += '<div class="form-check">' +
                '<input class="form-check-input ' + checkboxClass + '" type="checkbox" value="' + platformId + '" id="' + checkboxId + '"' + checked + '>' +
                '<label class="form-check-label" for="' + checkboxId + '">' + platformName + '</label>' +
                '</div>';
        });

        container.html(html || '<div class="text-muted small">No platforms available.</div>');
        $(submitButtonSelector).prop('disabled', html.length === 0);
    }

    function _setAddGamePlatformOptions(platforms) {
        _setGamePlatformOptions('#addGamePlatforms', 'add-game-platform', '#addGameSubmitBtn', platforms, []);
    }

    function _setEditGamePlatformOptions(platforms, selectedPlatformIds) {
        _setGamePlatformOptions('#editGamePlatforms', 'edit-game-platform', '#editGameSubmitBtn', platforms, selectedPlatformIds);
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

    function _openEditGameModal(gameId) {
        const game = _libraryGameById[gameId];
        if (!game) {
            _showAlert('#libraryAlert', 'Could not find this game to edit.', 'danger');
            return;
        }

        _editingGameId = Number(game.id);
        _editingGameCoverArtUrl = String(game.coverArtUrl || '');

        $('#editGameTitle').val(String(game.title || ''));
        $('#editGameYear').val(Number(game.releaseYear) || '');
        _hideAlert('#editGameModalAlert');
        $('#editGamePlatforms').html('<div class="text-muted small">Loading platforms...</div>');
        $('#editGameSubmitBtn').prop('disabled', true);
        bootstrap.Modal.getOrCreateInstance(document.getElementById('editGameModal')).show();

        $.ajax({
            method: 'GET',
            url: '/api/platforms',
            success: function (data) {
                const platforms = Array.isArray(data) ? data : [];
                const selectedPlatformIds = Array.isArray(game.platforms)
                    ? game.platforms.map(function (platform) { return Number(platform.id); }).filter(Boolean)
                    : [];
                _setEditGamePlatformOptions(platforms, selectedPlatformIds);
            },
            error: function () {
                _showAlert('#editGameModalAlert', 'Could not load platform options.', 'danger');
                _setEditGamePlatformOptions([], []);
            }
        });
    }

    function _submitEditGame() {
        const title = String($('#editGameTitle').val() || '').trim();
        const releaseYear = Number(String($('#editGameYear').val() || '').trim());
        const platformIds = $('.edit-game-platform:checked').map(function () {
            return Number($(this).val());
        }).get().filter(function (id) {
            return Number.isInteger(id) && id > 0;
        });

        if (!title) {
            _showAlert('#editGameModalAlert', 'Game name is required.', 'warning');
            return;
        }

        if (!Number.isInteger(releaseYear) || releaseYear < 0) {
            _showAlert('#editGameModalAlert', 'Release year must be a valid positive number.', 'warning');
            return;
        }

        if (platformIds.length === 0) {
            _showAlert('#editGameModalAlert', 'Select at least one platform.', 'warning');
            return;
        }

        $('#editGameSubmitBtn').prop('disabled', true);
        $.ajax({
            method: 'PUT',
            url: '/api/games/' + _editingGameId,
            contentType: 'application/json',
            data: JSON.stringify({
                title: title,
                coverArtUrl: _editingGameCoverArtUrl,
                releaseYear: releaseYear,
                platformIds: platformIds
            }),
            success: function () {
                bootstrap.Modal.getOrCreateInstance(document.getElementById('editGameModal')).hide();
                _showAlert('#libraryAlert', 'Game updated.', 'success');
                _loadLibrary();
            },
            error: function (xhr) {
                $('#editGameSubmitBtn').prop('disabled', false);
                if (xhr.status === 409) {
                    _showAlert('#editGameModalAlert', 'A game with that title already exists.', 'warning');
                    return;
                }
                if (xhr.status === 400) {
                    _showAlert('#editGameModalAlert', 'Invalid game details. Check title, year, and platform selection.', 'warning');
                    return;
                }
                _showAlert('#editGameModalAlert', 'Could not update game right now.', 'danger');
            }
        });
    }

    function _openConfirmActionModal(options) {
        $('#confirmActionTitle').text(options.title || 'Confirm Action');
        $('#confirmActionBody').text(options.message || 'Are you sure?');

        const confirmBtn = $('#confirmActionBtn');
        confirmBtn.removeClass('btn-danger btn-primary btn-warning btn-success')
            .addClass(options.confirmButtonClass || 'btn-danger')
            .text(options.confirmLabel || 'Confirm')
            .prop('disabled', false);

        _confirmActionHandler = typeof options.onConfirm === 'function' ? options.onConfirm : null;
        bootstrap.Modal.getOrCreateInstance(document.getElementById('confirmActionModal')).show();
    }

    function _deleteBacklogItem(backlogId) {
        const confirmBtn = $('#confirmActionBtn');
        confirmBtn.prop('disabled', true);

        $.ajax({
            method: 'DELETE',
            url: '/api/me/backlog/' + backlogId,
            success: function () {
                bootstrap.Modal.getOrCreateInstance(document.getElementById('confirmActionModal')).hide();
                _showAlert('#backlogAlert', 'Backlog item removed.', 'success');
                _loadBacklog();
            },
            error: function () {
                confirmBtn.prop('disabled', false);
                _showAlert('#backlogAlert', 'Could not remove backlog item.', 'danger');
            }
        });
    }

    function _deleteLibraryGame(gameId) {
        const confirmBtn = $('#confirmActionBtn');
        confirmBtn.prop('disabled', true);

        $.ajax({
            method: 'DELETE',
            url: '/api/games/' + gameId,
            success: function () {
                bootstrap.Modal.getOrCreateInstance(document.getElementById('confirmActionModal')).hide();
                _showAlert('#libraryAlert', 'Game deleted.', 'success');
                _loadLibrary();
            },
            error: function () {
                confirmBtn.prop('disabled', false);
                _showAlert('#libraryAlert', 'Could not delete game.', 'danger');
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

    function _wireEditGameModal() {
        $(document).on('click', '.library-edit', function () {
            if (!_isAdmin()) {
                return;
            }
            const gameId = Number($(this).data('id'));
            _openEditGameModal(gameId);
        });

        $('#editGameForm').on('submit', function (e) {
            e.preventDefault();
            _hideAlert('#editGameModalAlert');
            _submitEditGame();
        });

        $('#editGameModal').on('hidden.bs.modal', function () {
            _editingGameId = null;
            _editingGameCoverArtUrl = '';
            _hideAlert('#editGameModalAlert');
            $('#editGameForm')[0].reset();
            $('#editGameSubmitBtn').prop('disabled', false);
        });
    }

    function _wireConfirmActionModal() {
        $('#confirmActionBtn').on('click', function () {
            if (typeof _confirmActionHandler === 'function') {
                _confirmActionHandler();
            }
        });

        $('#confirmActionModal').on('hidden.bs.modal', function () {
            _confirmActionHandler = null;
            $('#confirmActionTitle').text('Confirm Action');
            $('#confirmActionBody').text('Are you sure?');
            $('#confirmActionBtn')
                .removeClass('btn-primary btn-warning btn-success')
                .addClass('btn-danger')
                .text('Confirm')
                .prop('disabled', false);
        });
    }

    function _wireAdminLibraryActions() {
        $(document).on('click', '.library-delete', function () {
            if (!_isAdmin()) {
                return;
            }
            const buttonEl = $(this);
            const gameId = Number(buttonEl.data('id'));
            const gameTitle = String(buttonEl.data('title') || 'this game');

            _openConfirmActionModal({
                title: 'Delete Game',
                message: 'Are you sure you want to delete "' + gameTitle + '" from the global library?',
                confirmLabel: 'Delete',
                confirmButtonClass: 'btn-danger',
                onConfirm: function () {
                    _deleteLibraryGame(gameId);
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
        _wireEditGameModal();
        _wireConfirmActionModal();

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
            const passwordPattern = /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d).{6,20}$/;

            // Client-side check before hitting the server
            if (password !== confirmPassword) {
                _showAlert('#authAlert', 'Passwords do not match.', 'danger');
                return;
            }

            if (!passwordPattern.test(password || '')) {
                _showAlert('#authAlert', 'Password must be 6-20 characters with uppercase, lowercase, and a number.', 'danger');
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

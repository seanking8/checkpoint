const App = (function () {

    let _token    = null;
    let _role     = null;
    let _username = null;

    function _showAlert(selector, message, type) {
        const el = $(selector);
        el.removeClass('d-none alert-success alert-danger alert-warning')
          .addClass('alert-' + (type || 'danger'))
          .text(message);
    }

    function _hideAlert(selector) {
        $(selector).addClass('d-none').text('');
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
    }

    // Boot. runs once on page load
    function _boot() {
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

            $.ajax({
                method: 'POST',
                url: '/api/auth/register',
                contentType: 'application/json',
                data: JSON.stringify({
                    username: $('#regUsername').val().trim(),
                    password: $('#regPassword').val()
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
        _clearToken();
        _showAuthView();
        showAuthTab('login');
    }

    // Init
    $(document).ready(_boot);

    // Public API
    return { showAuthTab, logout };

})();

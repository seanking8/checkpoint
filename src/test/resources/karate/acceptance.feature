Feature: API tests covering core user flows and admin catalog management

  Scenario: user can register, login, and fetch profile
    * def username = 'karate_' + java.lang.System.currentTimeMillis()

    Given url baseUrl
    And path 'api', 'auth', 'register'
    And request { username: '#(username)', password: 'secret123', confirmPassword: 'secret123' }
    When method post
    Then status 201

    Given url baseUrl
    And path 'api', 'auth', 'login'
    And request { username: '#(username)', password: 'secret123' }
    When method post
    Then status 200
    And match response.role == 'USER'
    * def userToken = response.token

    Given url baseUrl
    And path 'api', 'auth', 'me'
    And header Authorization = 'Bearer ' + userToken
    When method get
    Then status 200
    And match response.username == username
    And match response.role == 'USER'

  Scenario: registration fails when username already exists
    * def username = 'dupe_' + java.lang.System.currentTimeMillis()

    Given url baseUrl
    And path 'api', 'auth', 'register'
    And request { username: '#(username)', password: 'secret123', confirmPassword: 'secret123' }
    When method post
    Then status 201

    Given url baseUrl
    And path 'api', 'auth', 'register'
    And request { username: '#(username)', password: 'secret123', confirmPassword: 'secret123' }
    When method post
    Then status 409
    And match response.code == 'USERNAME_TAKEN'
    And match response.message == 'Username already taken'

  Scenario: authenticated user can browse global library
    * def username = 'library_' + java.lang.System.currentTimeMillis()

    Given url baseUrl
    And path 'api', 'auth', 'register'
    And request { username: '#(username)', password: 'secret123', confirmPassword: 'secret123' }
    When method post
    Then status 201

    Given url baseUrl
    And path 'api', 'auth', 'login'
    And request { username: '#(username)', password: 'secret123' }
    When method post
    Then status 200
    * def userToken = response.token

    Given url baseUrl
    And path 'api', 'games'
    And header Authorization = 'Bearer ' + userToken
    When method get
    Then status 200
    And match response == '#[]'
    And match response[*].title contains 'Elden Ring'

  Scenario: user can add game to backlog and duplicate add is rejected
    * def username = 'backlog_' + java.lang.System.currentTimeMillis()

    Given url baseUrl
    And path 'api', 'auth', 'register'
    And request { username: '#(username)', password: 'secret123', confirmPassword: 'secret123' }
    When method post
    Then status 201

    Given url baseUrl
    And path 'api', 'auth', 'login'
    And request { username: '#(username)', password: 'secret123' }
    When method post
    Then status 200
    * def userToken = response.token

    Given url baseUrl
    And path 'api', 'me', 'backlog'
    And header Authorization = 'Bearer ' + userToken
    And request { gameId: 1, platformId: 2 }
    When method post
    Then status 201

    Given url baseUrl
    And path 'api', 'me', 'backlog'
    And header Authorization = 'Bearer ' + userToken
    When method get
    Then status 200
    * def added = karate.filter(response, function(x){ return x.gameId == 1 && x.platformId == 2; })
    And match added[0].status == 'WANT_TO_PLAY'

    Given url baseUrl
    And path 'api', 'me', 'backlog'
    And header Authorization = 'Bearer ' + userToken
    And request { gameId: 1, platformId: 2 }
    When method post
    Then status 409

  Scenario: user can update backlog status
    * def username = 'status_' + java.lang.System.currentTimeMillis()

    Given url baseUrl
    And path 'api', 'auth', 'register'
    And request { username: '#(username)', password: 'secret123', confirmPassword: 'secret123' }
    When method post
    Then status 201

    Given url baseUrl
    And path 'api', 'auth', 'login'
    And request { username: '#(username)', password: 'secret123' }
    When method post
    Then status 200
    * def userToken = response.token

    Given url baseUrl
    And path 'api', 'me', 'backlog'
    And header Authorization = 'Bearer ' + userToken
    And request { gameId: 2, platformId: 3 }
    When method post
    Then status 201

    Given url baseUrl
    And path 'api', 'me', 'backlog'
    And header Authorization = 'Bearer ' + userToken
    When method get
    Then status 200
    * def matches = karate.filter(response, function(x){ return x.gameId == 2 && x.platformId == 3; })
    * def backlogId = matches[0].id

    Given url baseUrl
    And path 'api', 'me', 'backlog', backlogId, 'status'
    And header Authorization = 'Bearer ' + userToken
    And request { status: 'COMPLETED' }
    When method put
    Then status 204

    Given url baseUrl
    And path 'api', 'me', 'backlog'
    And header Authorization = 'Bearer ' + userToken
    When method get
    Then status 200
    * def updated = karate.filter(response, function(x){ return x.id == backlogId; })
    And match updated[0].status == 'COMPLETED'

  Scenario: admin can add edit and delete game in global catalog
    * def title = 'Karate Admin Game ' + java.lang.System.currentTimeMillis()

    Given url baseUrl
    And path 'api', 'auth', 'login'
    And request { username: 'dev_admin', password: 'dev_admin_password' }
    When method post
    Then status 200
    And match response.role == 'ADMIN'
    * def adminToken = response.token

    Given url baseUrl
    And path 'api', 'games'
    And header Authorization = 'Bearer ' + adminToken
    And request { title: '#(title)', coverArtUrl: '', releaseYear: 2024, platformIds: [1, 2] }
    When method post
    Then status 201
    * def gameId = response.id

    Given url baseUrl
    And path 'api', 'games', gameId
    And header Authorization = 'Bearer ' + adminToken
    And request { title: '#(title + " Updated")', coverArtUrl: '', releaseYear: 2025 }
    When method put
    Then status 200
    And match response.title == title + ' Updated'

    Given url baseUrl
    And path 'api', 'games', gameId
    And header Authorization = 'Bearer ' + adminToken
    When method delete
    Then status 204




Feature: Auth API smoke test

  Scenario: register, login, and fetch current user profile
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
    And match response.token == '#string'
    And match response.role == 'USER'
    And match response.username == username

    * def jwt = response.token

    Given url baseUrl
    And path 'api', 'auth', 'me'
    And header Authorization = 'Bearer ' + jwt
    When method get
    Then status 200
    And match response.username == username
    And match response.role == 'USER'


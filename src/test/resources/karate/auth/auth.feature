Feature: Auth tests

  Scenario: user can register, login, and fetch profile
    * def auth = call read('classpath:karate/common/auth.feature')

    Given url baseUrl
    And path 'api', 'auth', 'me'
    And header Authorization = 'Bearer ' + auth.token
    When method get
    Then status 200
    And match response.username == auth.username
    And match response.role == 'USER'

  Scenario: registration fails when username already exists
    * def uid = '' + java.lang.Math.floor(java.lang.Math.random() * 1000000000)
    * def username = 'd' + ('000000000' + uid).slice(-9)

    Given url baseUrl
    And path 'api', 'auth', 'register'
    And request { username: '#(username)', password: 'Secret1', confirmPassword: 'Secret1' }
    When method post
    Then status 201

    Given url baseUrl
    And path 'api', 'auth', 'register'
    And request { username: '#(username)', password: 'Secret1', confirmPassword: 'Secret1' }
    When method post
    Then status 409
    And match response.code == 'USERNAME_TAKEN'
    And match response.message == 'Username already taken'


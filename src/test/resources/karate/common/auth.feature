Feature: Auth helper flows

  Scenario: register and login a unique USER
    * def username = 'karate_' + java.lang.System.currentTimeMillis() + '_' + java.util.UUID.randomUUID().toString().replace('-', '')

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
    * def token = response.token


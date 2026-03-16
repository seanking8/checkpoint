Feature: Auth helper flows

  Scenario: register and login a unique USER
    * def uid = '' + java.lang.Math.floor(java.lang.Math.random() * 1000000000)
    * def username = 'u' + ('000000000' + uid).slice(-9)

    Given url baseUrl
    And path 'api', 'auth', 'register'
    And request { username: '#(username)', password: 'Secret1', confirmPassword: 'Secret1' }
    When method post
    Then status 201

    Given url baseUrl
    And path 'api', 'auth', 'login'
    And request { username: '#(username)', password: 'Secret1' }
    When method post
    Then status 200
    * def token = response.token


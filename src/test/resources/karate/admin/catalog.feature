Feature: Admin catalog tests

  Scenario: admin can add, edit, and delete a game in the master catalog
    * def uniqueTitle = 'Karate Admin Game ' + java.lang.System.currentTimeMillis()

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
    And request { title: '#(uniqueTitle)', coverArtUrl: '', releaseYear: 2024, platformIds: [1, 2] }
    When method post
    Then status 201
    * def gameId = response.id

    Given url baseUrl
    And path 'api', 'games', gameId
    And header Authorization = 'Bearer ' + adminToken
    And request { title: '#(uniqueTitle + " Updated")', coverArtUrl: '', releaseYear: 2025 }
    When method put
    Then status 200
    And match response.title == uniqueTitle + ' Updated'
    And match response.releaseYear == 2025

    Given url baseUrl
    And path 'api', 'games', gameId
    And header Authorization = 'Bearer ' + adminToken
    When method delete
    Then status 204

  Scenario: non-admin user cannot create a global game
    * def auth = call read('classpath:karate/common/auth.feature')

    Given url baseUrl
    And path 'api', 'games'
    And header Authorization = 'Bearer ' + auth.token
    And request { title: 'Forbidden create', coverArtUrl: '', releaseYear: 2024, platformIds: [1] }
    When method post
    Then status 403


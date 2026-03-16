Feature: Backlog tests

  Scenario: user adds a game to backlog and sees default WANT_TO_PLAY status
    * def auth = call read('classpath:karate/common/auth.feature')

    Given url baseUrl
    And path 'api', 'me', 'backlog'
    And header Authorization = 'Bearer ' + auth.token
    And request { gameId: 1, platformId: 2 }
    When method post
    Then status 201

    Given url baseUrl
    And path 'api', 'me', 'backlog'
    And header Authorization = 'Bearer ' + auth.token
    When method get
    Then status 200
    * def added = karate.filter(response, function(x){ return x.gameId == 1 && x.platformId == 2; })
    And match added[0].status == 'WANT_TO_PLAY'

  Scenario: duplicate game-platform backlog add is rejected
    * def auth = call read('classpath:karate/common/auth.feature')

    Given url baseUrl
    And path 'api', 'me', 'backlog'
    And header Authorization = 'Bearer ' + auth.token
    And request { gameId: 1, platformId: 2 }
    When method post
    Then status 201

    Given url baseUrl
    And path 'api', 'me', 'backlog'
    And header Authorization = 'Bearer ' + auth.token
    And request { gameId: 1, platformId: 2 }
    When method post
    Then status 409

  Scenario: user can update backlog status without reloading page
    * def auth = call read('classpath:karate/common/auth.feature')

    Given url baseUrl
    And path 'api', 'me', 'backlog'
    And header Authorization = 'Bearer ' + auth.token
    And request { gameId: 2, platformId: 3 }
    When method post
    Then status 201

    Given url baseUrl
    And path 'api', 'me', 'backlog'
    And header Authorization = 'Bearer ' + auth.token
    When method get
    Then status 200
    * def matches = karate.filter(response, function(x){ return x.gameId == 2 && x.platformId == 3; })
    * def backlogId = matches[0].id

    Given url baseUrl
    And path 'api', 'me', 'backlog', backlogId, 'status'
    And header Authorization = 'Bearer ' + auth.token
    And request { status: 'COMPLETED' }
    When method put
    Then status 204

    Given url baseUrl
    And path 'api', 'me', 'backlog'
    And header Authorization = 'Bearer ' + auth.token
    When method get
    Then status 200
    * def updated = karate.filter(response, function(x){ return x.id == backlogId; })
    And match updated[0].status == 'COMPLETED'


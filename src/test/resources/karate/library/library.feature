Feature: Global library tests

  Scenario: authenticated user can browse global library
    * def auth = call read('classpath:karate/common/auth.feature')

    Given url baseUrl
    And path 'api', 'games'
    And header Authorization = 'Bearer ' + auth.token
    When method get
    Then status 200
    And match response == '#[]'
    And match response[*].title contains 'Elden Ring'
    And match response[*].title contains 'Hades'

  Scenario: unauthenticated request to global library is rejected
    Given url baseUrl
    And path 'api', 'games'
    When method get
    Then status 401


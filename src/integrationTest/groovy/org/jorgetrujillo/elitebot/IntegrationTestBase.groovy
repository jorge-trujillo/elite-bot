package org.jorgetrujillo.elitebot

import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ContextConfiguration
import spock.lang.Specification
import spock.lang.Stepwise

@SpringBootTest(classes = [Application])
@ContextConfiguration(classes = [Application])
@Stepwise
class IntegrationTestBase extends Specification {
}

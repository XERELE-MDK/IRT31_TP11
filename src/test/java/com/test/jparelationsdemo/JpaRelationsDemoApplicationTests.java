package com.test.jparelationsdemo;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test") // désactive le CommandLineRunner (voir @Profile("!test"))
class JpaRelationsDemoApplicationTests {

    @Test
    void contextLoads() {
    }

}

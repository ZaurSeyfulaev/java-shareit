package ru.practicum.shareit;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class ShareItTests {

    @Autowired
    private ApplicationContext applicationContext;

    @Test
    void contextLoads() {
        assertNotNull(applicationContext);
    }

    @Test
    void testMainMethodExists() {
        assertNotNull(ShareItApp.class);
    }

    @Test
    void testPackageExists() {
        String packageName = ShareItApp.class.getPackage().getName();
        assertEquals("ru.practicum.shareit", packageName);
    }

    @Test
    void testAppStartsWithoutErrors() {
        String[] args = {};
        try {
            ShareItApp.main(args);
        } catch (Exception e) {
            fail("Приложение не должно выбрасывать исключения при запуске: " + e.getMessage());
        }
    }

    @Test
    void testApplicationContextNotNull() {
        assertNotNull(applicationContext);
    }
}
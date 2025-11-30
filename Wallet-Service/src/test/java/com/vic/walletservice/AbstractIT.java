package com.vic.walletservice;


import com.vic.walletservice.Services.walletService;
import io.restassured.RestAssured;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;



@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public abstract class AbstractIT {

    @MockitoBean
    protected walletService walletService;


    @LocalServerPort
    private int port;

    @BeforeEach
    void setupRestAssured() {
        RestAssured.baseURI = "http://localhost:" + port + "/api";
        RestAssured.port = port;
    }

}


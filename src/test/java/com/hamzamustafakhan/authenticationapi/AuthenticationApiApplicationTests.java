package com.hamzamustafakhan.authenticationapi;

import com.hamzamustafakhan.authenticationapi.dto.UserInfoDTO;
import com.hamzamustafakhan.authenticationapi.service.UserAuthenticationService;
import com.hamzamustafakhan.authenticationapi.utils.Constants;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.junit.Assert;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultMatcher;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.util.HashSet;

import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Slf4j
@SpringBootTest
@AutoConfigureMockMvc
@RunWith(SpringRunner.class)
class AuthenticationApiApplicationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserAuthenticationService service;


    @Test
    void contextLoads() {
    }

    @Test
    @SneakyThrows
    @WithMockUser(username = "hamza@gmail.com")
    void getUserInfoTest(){
        log.info("Testing: user info API");
        mockMvc.perform(MockMvcRequestBuilders.post("/api/auth/user")
                .content("{\n" +
                        "    \"hamza@gmail.com\"\n" +
                        "}")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(ResultMatcher.matchAll(status().isOk()))
                .andDo(print());
    }

    @ParameterizedTest
    @ValueSource(strings = {"hmkscorpio@gmail.com"})
    @SneakyThrows
    @WithAnonymousUser
    void createUserTest(String email){
        if(email == null){
            email = "hmkscorpio@gmail.com";
        }
        log.info("Testing: sign up API");
        mockMvc.perform(MockMvcRequestBuilders.post("/api/auth/signup")
                        .content("{\n" +
                                "    \"email\":" + "\"" + email + "\"" +",\n" +
                                "    \"password\":\"123456\",\n" +
                                "    \"name\":\"Hamza Khan\"\n" +
                                "}")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.response").value(Constants.SUCCESS))
                .andDo(print());
    }

    @Test
    @SneakyThrows
    void signInTest(){
        log.info("Testing: sign in API");
        mockMvc.perform(MockMvcRequestBuilders.post("/api/auth/signin")
                .content("{\n" +
                        "    \"email\":\"hamza@gmail.com\",\n" +
                        "    \"password\":\"1234567\"\n" +
                        "}")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.status").value(Constants.SUCCESS))
                .andDo(print());
    }

    @Test
    @SneakyThrows
    void requestResetPMasswordTest(){
        log.info("Testing: request reset password API");
        createUserTest("khanhamzamustafa@gmail.com");
        mockMvc.perform(MockMvcRequestBuilders.post("/api/auth/request-reset-password")
                .content("{\n" +
                         "   \"khanhamzamustafa@gmail.com\"\n" +
                        "}")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.status").value(Constants.SUCCESS))
                .andDo(print());
    }

    @Test
    @SneakyThrows
    void approveRequestResetPasswordTest(){
        log.info("Testing: approve request reset password API");
        mockMvc.perform(MockMvcRequestBuilders.get("/api/auth/approve-request-reset-password/"+99))
                .andExpect(status().isNotFound())
                .andExpect(MockMvcResultMatchers.jsonPath("$['response'].message").value(Constants.REQUEST_NOT_FOUND))
                .andDo(print());
    }

    @Test
    @SneakyThrows
    void resetPasswordTest(){
        log.info("Testing: reset password API");
        mockMvc.perform(MockMvcRequestBuilders.put("/api/auth/reset-password")
                        .content("{\n" +
                                "    \"email\":\"hamza@gmail.com\",\n" +
                                "    \"password\":\"12345657\"\n" +
                                "}")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotAcceptable())
                .andExpect(MockMvcResultMatchers.jsonPath("$['response'].message").value(Constants.SUBMIT_RESET_REQUEST))
                .andDo(print());
    }

    @Test
    @SneakyThrows
    void updateFailedAttemptTest(){
        log.info("Testing: sign in API for failure");
        mockMvc.perform(MockMvcRequestBuilders.post("/api/auth/signin")
                        .content("{\n" +
                                "    \"email\":\"hamza@gmail.com\",\n" +
                                "    \"password\":\"123456887\"\n" +
                                "}")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isExpectationFailed())
                .andExpect(MockMvcResultMatchers.jsonPath("$['response'].message").value(Constants.INVALID_CREDENTIALS))
                .andDo(print());
    }

    @Test
    @SneakyThrows
    void userDetailsService(){
        log.info("Testing: user details method");
        UserDetails expectedUserDetails = new User("hamza@gmail.com", "$2a$10$2lkpCTlf7/5DILb30XvhCuznO3afJSGA55hne0yQBAs0xfRwoqL2O", new HashSet<SimpleGrantedAuthority>());
        UserDetails actualUserDetails = service.loadUserByUsername("hamza@gmail.com");
        Assert.assertEquals(expectedUserDetails, actualUserDetails);
    }

    @Test
    @SneakyThrows
    void userInfoMethodTest(){
        log.info("Testing: user info method");
        UserInfoDTO userInfoDTOExpected = new UserInfoDTO("hamza@gmail.com", "Hamza Mustafa Khan");
        UserInfoDTO userInfoDTOActual = service.getUserInfo("hamza@gmail.com");

        Assert.assertEquals(userInfoDTOExpected, userInfoDTOActual);
    }

    @Test
    @SneakyThrows
    void createUserMethodTest(){
        log.info("Testing: user signup method ");
        com.hamzamustafakhan.authenticationapi.entity.User user = new com.hamzamustafakhan.authenticationapi.entity.User();
        user.setEmail("test@gmail.com");
        user.setPassword("1234567");
        user.setName("Test User");
        String response = service.createUser(user);
        Assert.assertEquals(response, Constants.SUCCESS);

    }

    @Test
    @SneakyThrows
    void updateFailedCountMethodTest(){
        log.info("Testing: user failed attempt method ");
        int count = service.updateFailedAttemptCount("hamza@gmail.com", false);
        Assert.assertEquals(count, 1);
    }


    @Test
    @SneakyThrows
    void authenticationMethodTest(){
        log.info("Testing: user signin method ");
        String response = service.authenticateUser("hamza@gmail.com","1234567");
        Assert.assertEquals(response, Constants.SUCCESS);
    }

}

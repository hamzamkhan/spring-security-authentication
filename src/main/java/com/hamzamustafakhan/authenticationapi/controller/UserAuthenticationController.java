package com.hamzamustafakhan.authenticationapi.controller;

import com.hamzamustafakhan.authenticationapi.domain.GenericResponse;
import com.hamzamustafakhan.authenticationapi.dto.AuthenticationRequestDTO;
import com.hamzamustafakhan.authenticationapi.dto.UserInfoDTO;
import com.hamzamustafakhan.authenticationapi.entity.User;
import com.hamzamustafakhan.authenticationapi.exception.ServiceLayerException;
import com.hamzamustafakhan.authenticationapi.service.UserAuthenticationService;
import com.hamzamustafakhan.authenticationapi.utils.Constants;
import com.hamzamustafakhan.authenticationapi.config.TokenUtil;
import com.hamzamustafakhan.authenticationapi.utils.Roles;
import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
@Api(tags = "Authentication APIs")
public class UserAuthenticationController {

    private final TokenUtil tokenUtil;
    private final UserAuthenticationService userAuthenticationService;

    UserAuthenticationController(TokenUtil tokenUtil, UserAuthenticationService userAuthenticationService){
        this.tokenUtil = tokenUtil;
        this.userAuthenticationService = userAuthenticationService;
    }

    @PostMapping("/signup")
    public ResponseEntity<GenericResponse> createUser(@Valid @RequestBody User user) throws Exception {
        GenericResponse<String> genericResponse = new GenericResponse<>();
        String response = userAuthenticationService.createUser(user);
        genericResponse.setResponse(response);
        genericResponse.setStatus(Constants.SUCCESS);
        return new ResponseEntity<>(genericResponse, HttpStatus.OK);
    }

    @PostMapping("/signin")
    public ResponseEntity<GenericResponse> authenticateUser(@Valid @RequestBody AuthenticationRequestDTO requestDTO) throws UnsupportedEncodingException, Exception {
        GenericResponse<String> genericResponse = new GenericResponse<>();
        userAuthenticationService.authenticateUser(requestDTO.getEmail(), requestDTO.getPassword());
        UserDetails userDetails = userAuthenticationService.loadUserByUsername(requestDTO.getEmail());
        User user = userAuthenticationService.getUserDetails(requestDTO.getEmail());
        int roleId = user.getRole();
        String roleName = null;
        if(roleId == Roles.USER.getId()){
            roleName = Roles.USER.getLabel();
        } else {
            roleName = Roles.ADMIN.getLabel();;
        }
        List<GrantedAuthority> grantedAuthorityList = new ArrayList<>(userDetails.getAuthorities());

        SimpleGrantedAuthority simpleGrantedAuthority = new SimpleGrantedAuthority(roleName);
        grantedAuthorityList.add(simpleGrantedAuthority);
        String token = tokenUtil.createToken(userDetails, user.getId(), roleName);
        genericResponse.setResponse(token);
        genericResponse.setStatus(Constants.SUCCESS);
        return new ResponseEntity<>(genericResponse, HttpStatus.OK);
    }


    @PostMapping("/user")
    public ResponseEntity<GenericResponse> retriveUserInfo(@RequestBody String email) throws Exception{
        GenericResponse<UserInfoDTO> genericResponse = new GenericResponse<>();
        email = userAuthenticationService.parseEmail(email);
        UserInfoDTO response = userAuthenticationService.getUserInfo(email);
        genericResponse.setResponse(response);
        genericResponse.setStatus(Constants.SUCCESS);
        return new ResponseEntity<>(genericResponse, HttpStatus.OK);
    }

    @PostMapping("/request-reset-password")
    public ResponseEntity<GenericResponse> requestResetPassword(@RequestBody String email, HttpServletRequest request) throws Exception{
        email = userAuthenticationService.parseEmail(email);
        String siteURL = request.getRequestURL().toString();
        siteURL = siteURL.replace(request.getServletPath(), "");
        GenericResponse<String> genericResponse = new GenericResponse<>();

        String response = userAuthenticationService.requestPasswordReset(email, siteURL);
        genericResponse.setResponse(response);
        genericResponse.setStatus(Constants.SUCCESS);
        return new ResponseEntity<>(genericResponse, HttpStatus.OK);
    }

    @GetMapping("/approve-request-reset-password/{id}")
    public ResponseEntity<GenericResponse> approveRequestResetPassword(@PathVariable int id){
        GenericResponse<String> genericResponse = new GenericResponse<>();

        String response = userAuthenticationService.approveResetRequest(id);
        genericResponse.setResponse(response);
        genericResponse.setStatus(Constants.SUCCESS);
        return new ResponseEntity<>(genericResponse, HttpStatus.OK);
    }

    @PutMapping("/reset-password")
    public ResponseEntity<GenericResponse> resetPassword(@RequestBody AuthenticationRequestDTO requestDTO){
        GenericResponse<String> genericResponse = new GenericResponse<>();

        String response = userAuthenticationService.resetPassword(requestDTO.getEmail(), requestDTO.getPassword());
        genericResponse.setResponse(response);
        genericResponse.setStatus(Constants.SUCCESS);
        return new ResponseEntity<>(genericResponse, HttpStatus.OK);
    }


}

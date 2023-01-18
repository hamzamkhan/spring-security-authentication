package com.hamzamustafakhan.authenticationapi.service;

import com.hamzamustafakhan.authenticationapi.config.TokenUtil;
import com.hamzamustafakhan.authenticationapi.dao.ResetPasswordRequestDAO;
import com.hamzamustafakhan.authenticationapi.dao.UserDAO;
import com.hamzamustafakhan.authenticationapi.dto.UserInfoDTO;
import com.hamzamustafakhan.authenticationapi.entity.ResetPasswordRequest;
import com.hamzamustafakhan.authenticationapi.entity.User;
import com.hamzamustafakhan.authenticationapi.exception.ServiceLayerException;
import com.hamzamustafakhan.authenticationapi.utils.Constants;
import com.hamzamustafakhan.authenticationapi.utils.Roles;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import javax.mail.internet.MimeMessage;
import java.util.Date;
import java.util.HashSet;
import java.util.Optional;

@Service
public class UserAuthenticationService implements UserDetailsService {

    private final UserDAO userDAO;

    private final PasswordEncoder encoder;

    private final AuthenticationManager manager;

    @Value("${spring.mail.username}")
    private String resetMail;

    @Value("${failed.attempt.limit}")
    private int failedAttemptLimit;

    private final JavaMailSender javaMailSender;

    private final TokenUtil tokenUtil;

    private final ResetPasswordRequestDAO resetPasswordRequestDAO;

    UserAuthenticationService(UserDAO userDAO, PasswordEncoder passwordEncoder, AuthenticationManager manager, JavaMailSender javaMailSender, TokenUtil tokenUtil, ResetPasswordRequestDAO resetPasswordRequestDAO){
        this.userDAO = userDAO;
        this.encoder = passwordEncoder;
        this.manager = manager;
        this.tokenUtil = tokenUtil;
        this.resetPasswordRequestDAO = resetPasswordRequestDAO;
        this.javaMailSender = javaMailSender;
    }


    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userDAO.findByEmail(email);
        if(user != null){
            return new org.springframework.security.core.userdetails.User(user.getEmail(), user.getPassword(), new HashSet<SimpleGrantedAuthority>());
        }
        return null;
    }

    public int updateFailedAttemptCount(String email, boolean resetCount) throws ServiceLayerException {
        User user = userDAO.findByEmail(email);
        if(user == null){
            throw new ServiceLayerException(Constants.USER_NOT_FOUND, HttpStatus.NOT_FOUND);
        }
        int failedAttemptCount = 0;
        if(!resetCount){
            if(user.getFailedAttempts() < failedAttemptLimit){
                failedAttemptCount = user.getFailedAttempts();
                failedAttemptCount+=1;
                user.setFailedAttempts(failedAttemptCount);
                if(failedAttemptCount >= failedAttemptLimit){
                    user.setStatus(Constants.INACTIVE);
                }
            }
        } else {
            user.setFailedAttempts(0);
        }
        user.setUpdatedAt(new Date());
        userDAO.save(user);
        return failedAttemptCount;
    }

    public String createUser(User user) throws ServiceLayerException {
        User existingUser = userDAO.findByEmail(user.getEmail());
        if(existingUser != null){
            throw new ServiceLayerException(Constants.USER_EXISTS, HttpStatus.NOT_ACCEPTABLE);
        }
        String encryptedPassword = encoder.encode(user.getPassword());
        user.setPassword(encryptedPassword);
        user.setCreatedAt(new Date());
        user.setFailedAttempts(0);
        user.setStatus(Constants.ACTIVE);
        user.setRole(Roles.USER.getId());
        userDAO.save(user);
        return Constants.SUCCESS;
    }

    public String authenticateUser(String email, String password) throws Exception {
        try{
            User user = userDAO.findByEmail(email);
            if(user == null){
                throw new ServiceLayerException(Constants.USER_NOT_FOUND, HttpStatus.NOT_FOUND);
            }
            if(user.getStatus().equalsIgnoreCase(Constants.INACTIVE)){
                throw new ServiceLayerException(Constants.USER_DISABLED, HttpStatus.EXPECTATION_FAILED);
            }
            if(resetPasswordRequestDAO.findByUser(user.getId(), Constants.APPROVED) != null){
                throw new ServiceLayerException(Constants.RESET_PASSWORD, HttpStatus.NOT_ACCEPTABLE);
            }
            manager.authenticate(new UsernamePasswordAuthenticationToken(email, password));
            updateFailedAttemptCount(email, true);
        } catch(BadCredentialsException|ServiceLayerException e){
            ServiceLayerException serviceLayerException = null;
            if(e.getClass().getSimpleName().contains("ServiceLayerException")){
                serviceLayerException = (ServiceLayerException) e;
            }

            int count = updateFailedAttemptCount(email, false);
            if(count >= failedAttemptLimit && serviceLayerException == null){
                throw new ServiceLayerException(Constants.INVALID_CREDENTIALS_RESET, HttpStatus.EXPECTATION_FAILED);
            }
            if(serviceLayerException == null || serviceLayerException.getMessage().equalsIgnoreCase(Constants.INVALID_CREDENTIALS)){
                throw new ServiceLayerException(Constants.INVALID_CREDENTIALS, HttpStatus.EXPECTATION_FAILED);
            } else {
                throw new ServiceLayerException(serviceLayerException.getMessage(), serviceLayerException.getHttpStatus());
            }

        }
        return Constants.SUCCESS;
    }

    public User getUserDetails(String email) {
        User user = userDAO.findByEmail(email);
        return user;
    }

    public UserInfoDTO getUserInfo(String email) throws ServiceLayerException {
        User user = userDAO.findByEmail(email);
        UserInfoDTO userInfoDTO = new UserInfoDTO();
        if(user == null){
            throw new ServiceLayerException(Constants.USER_NOT_FOUND, HttpStatus.NOT_FOUND);
        }
        userInfoDTO.setName(user.getName());
        userInfoDTO.setEmail(user.getEmail());
        return userInfoDTO;
    }

    public String requestPasswordReset(String email, String link) throws Exception{
            User user = userDAO.findByEmail(email);
            if(user == null){
                throw new ServiceLayerException(Constants.USER_NOT_FOUND, HttpStatus.NOT_FOUND);
            }
            ResetPasswordRequest resetPasswordRequest = resetPasswordRequestDAO.findByUser(user.getId(), Constants.PENDING);
            if(resetPasswordRequest != null){
                throw new ServiceLayerException(Constants.REQUEST_EXISTS, HttpStatus.NOT_ACCEPTABLE);
            }
            ResetPasswordRequest approvedResetPasswordRequest = resetPasswordRequestDAO.findByUser(user.getId(), Constants.APPROVED);
            if(approvedResetPasswordRequest != null){
                throw new ServiceLayerException(Constants.REQUEST_ALREADY_APPROVED, HttpStatus.NOT_ACCEPTABLE);
            }
            ResetPasswordRequest request = new ResetPasswordRequest();
            request.setUser(user);
            request.setStatus(Constants.PENDING);
            request.setCreatedAt(new Date());
            ResetPasswordRequest response = resetPasswordRequestDAO.save(request);
            if(response != null){
                String url = link + "/api/auth/approve-request-reset-password/" + response.getId();
                String content = "<p>Hi, " + user.getName() + "</p>"
                        + "<p> Your reset password request has been received, click the link below to approve it and then call /api/auth/reset-password with your email and new password</p>"
                        + "<p><a href=\"" + url + "\"> Approve Reset Request</a></p>"
                        +"<br>";

                MimeMessage message = javaMailSender.createMimeMessage();
                MimeMessageHelper helper = new MimeMessageHelper(message);
                helper.setFrom(resetMail);
                helper.setTo(user.getEmail());
                helper.setSubject("Reset Password Request Received");
                helper.setText(content, true);

                javaMailSender.send(message);

                user.setStatus(Constants.INACTIVE);
                userDAO.save(user);

            }

        return Constants.SUCCESS;
    }

    public String approveResetRequest(int id){
        Optional<ResetPasswordRequest> optional = resetPasswordRequestDAO.findById(id);
        if(!optional.isPresent()){
            throw new ServiceLayerException(Constants.REQUEST_NOT_FOUND, HttpStatus.NOT_FOUND);
        }
        ResetPasswordRequest request = optional.get();
        if(request.getStatus().equalsIgnoreCase(Constants.APPROVED)){
            throw new ServiceLayerException(Constants.REQUEST_ALREADY_APPROVED, HttpStatus.IM_USED);
        }
        request.setStatus(Constants.APPROVED);
        request.setUpdatedAt(new Date());
        User user = request.getUser();
        user.setStatus(Constants.ACTIVE);
        userDAO.save(user);
        resetPasswordRequestDAO.save(request);
        return Constants.APPROVED;
    }

    public String resetPassword(String email, String password){
        User user = userDAO.findByEmail(email);
        if(user == null){
            throw new ServiceLayerException(Constants.USER_NOT_FOUND, HttpStatus.NOT_FOUND);
        }

        if(user.getStatus().equalsIgnoreCase(Constants.INACTIVE)){
            throw new ServiceLayerException(Constants.USER_DISABLED, HttpStatus.NOT_ACCEPTABLE);
        }
        ResetPasswordRequest request = resetPasswordRequestDAO.findByUser(user.getId(), Constants.PENDING);
        if(request != null){
            throw new ServiceLayerException(Constants.REQUEST_EXISTS, HttpStatus.NOT_ACCEPTABLE);
        }
        ResetPasswordRequest approvedRequest = resetPasswordRequestDAO.findByUser(user.getId(), Constants.APPROVED);
        if(user.getStatus().equalsIgnoreCase(Constants.ACTIVE) && approvedRequest==null){
            throw new ServiceLayerException(Constants.SUBMIT_RESET_REQUEST, HttpStatus.NOT_ACCEPTABLE);
        }
        String encryptedPassword = encoder.encode(password);
        user.setPassword(encryptedPassword);
        user.setUpdatedAt(new Date());
        user.setStatus(Constants.ACTIVE);
        user.setFailedAttempts(0);
        userDAO.save(user);
        resetPasswordRequestDAO.deleteRequests(user.getId());
        return Constants.SUCCESS;
    }

    public String parseEmail(String email){
        email = email.replace("\"","");
        email = email.replace("\n", "");
        email = email.replace("{","");
        email = email.replace("}","");
        email = email.trim();
        return email;
    }
}

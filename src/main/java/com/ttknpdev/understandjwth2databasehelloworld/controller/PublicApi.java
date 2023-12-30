package com.ttknpdev.understandjwth2databasehelloworld.controller;

import com.ttknpdev.understandjwth2databasehelloworld.configuration.jwt.JwtTokenUtil;
import com.ttknpdev.understandjwth2databasehelloworld.dao.UserDao;
import com.ttknpdev.understandjwth2databasehelloworld.entities.User;
import com.ttknpdev.understandjwth2databasehelloworld.entities.jwt.JwtRequest;
import com.ttknpdev.understandjwth2databasehelloworld.entities.jwt.JwtResponse;
import com.ttknpdev.understandjwth2databasehelloworld.log.Logging;
import com.ttknpdev.understandjwth2databasehelloworld.service.JwtUserDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/jwt")
public class PublicApi {
    private AuthenticationManager authenticationManager;
    private JwtTokenUtil jwtTokenUtil;
    private JwtUserDetailsService userDetailsService;
    private UserDao userDao;
    private Logging logging;
    @Autowired
    public PublicApi(AuthenticationManager authenticationManager,
                               @Qualifier("tokenUtil") JwtTokenUtil jwtTokenUtil,
                               @Qualifier("detailsService") JwtUserDetailsService userDetailsService,
                               UserDao userDao) {
        this.authenticationManager = authenticationManager;
        this.jwtTokenUtil = jwtTokenUtil;
        this.userDetailsService = userDetailsService;
        this.userDao = userDao;
        logging = new Logging(this.getClass());
    }

    // method for request token and check username has alive
    @PostMapping(value = "/")
    public ResponseEntity<?> createAuthenticationToken(@RequestBody JwtRequest jwtRequest) throws Exception {

        logging.logBack.info("http://localhost:8080/jwt/ is accessed (Public API)");

        authenticate( jwtRequest.getUsername() , jwtRequest.getPassword() );

        final UserDetails USER_DETAILS = userDetailsService.loadUserByUsername(jwtRequest.getUsername()); // retrieve the user from database

        final String TOKEN = jwtTokenUtil.generateToken(USER_DETAILS);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(new JwtResponse(TOKEN));
    }

    @PostMapping(value = "/create")
    public ResponseEntity<?> saveUser(@RequestBody User user) throws Exception {
        logging.logBack.info("http://localhost:8080/register/create is accessed (Public API)");
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(userDao.create(user));
    }
    private void authenticate(String username, String password) throws Exception {

        try {

            UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken = new UsernamePasswordAuthenticationToken(username, password);
            authenticationManager.authenticate(usernamePasswordAuthenticationToken);

        } catch (DisabledException e) {

            throw new Exception("User disabled", e.getCause());

        } catch (BadCredentialsException e) {
            /* If username and password is not correct it will find this exception */

            throw new Exception("Invalid credentials", e.getCause());

        }
    }
}

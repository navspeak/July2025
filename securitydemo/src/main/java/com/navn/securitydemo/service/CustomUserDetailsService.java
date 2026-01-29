package com.navn.securitydemo.service;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@ConditionalOnProperty(
        name = "customUserDetailsService",
        havingValue = "true",
        matchIfMissing = false
)
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepo;
    private final AuthRepository authRepo;

    public CustomUserDetailsService(UserRepository repo, AuthRepository authRepo) {
        this.userRepo = repo;
        this.authRepo = authRepo;
    }

    @Override
    public UserDetails loadUserByUsername(String username) {
        AppUser user = userRepo.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        return User.withUsername(user.userName())
                .password(user.password())
                .roles(user.roles.toArray(new String[0]))
                .build();
    }


    //override
    public UserDetails loadUserByUsername_v2(String username) {
        AppUser user = userRepo.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        List<String> authStrings = authRepo.findAuthoritiesByUserId(user.id());

        List<SimpleGrantedAuthority> authorities = authStrings.stream()
                .map(SimpleGrantedAuthority::new)
                .toList();

        return new User(
                user.userName(),
                user.password(),
                authorities
        );
    }

    /*------------------*/
    // These would be autowired but static class for ease of reference
    @Service
    @ConditionalOnProperty(
            name = "customUserDetailsService",
            havingValue = "true",
            matchIfMissing = false
    )
    public static class UserRepository {
        Optional<AppUser> findByUsername(String name){
            return Optional.of(
                    new AppUser(1,"John",
                    "$2a$10$7Q9...<bcrypt>",
                    List.of("USER", "ADMIN"))); //Spring Adds ROLE_ so ensure you dont store with ROLE_
        }
    }

    @Service
    @ConditionalOnProperty(
            name = "customUserDetailsService",
            havingValue = "true",
            matchIfMissing = false
    )
    public static class AuthRepository {
        public List<String> findAuthoritiesByUserId(Integer id) {
            return List.of("USER", "ADMIN");
        }
    }


    @Component
    @ConditionalOnProperty(
            name = "customUserDetailsService",
            havingValue = "true",
            matchIfMissing = false
    )
    record AppUser(Integer id, String userName, String password, List<String> roles){}
    /*------------------*/
}

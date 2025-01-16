//package org.example.autopark.simpleUser;
//
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.security.core.userdetails.UserDetails;
//import org.springframework.security.core.userdetails.UserDetailsService;
//import org.springframework.security.core.userdetails.UsernameNotFoundException;
//import org.springframework.stereotype.Service;
//
//import java.util.Optional;
//
//@Service
//public class SimpleUserDetailsService implements UserDetailsService {
//    private final SimpleUserRepository simpleUserRepository;
//
//    @Autowired
//    public SimpleUserDetailsService(SimpleUserRepository simpleUserRepository) {
//        this.simpleUserRepository = simpleUserRepository;
//    }
//
//
//    @Override
//    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
//        Optional<SimpleUser> simpleUser = simpleUserRepository.findByUsername(username);
//        if (simpleUser.isEmpty()) {
//            throw new UsernameNotFoundException("USer not found");
//        }
//        return new SimpleUserDetails(simpleUser.get());
//    }
//}

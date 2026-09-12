package org.sid.restaurationbackend.security.userdetails;

import org.sid.restaurationbackend.entities.ClientAuthentifie;
import org.sid.restaurationbackend.repositories.ClientAuthentifieRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ClientUserDetailsService implements UserDetailsService {

    private final ClientAuthentifieRepository clientRepository;

    public ClientUserDetailsService(
            ClientAuthentifieRepository clientRepository
    ) {
        this.clientRepository = clientRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String email)
            throws UsernameNotFoundException {

        ClientAuthentifie client = clientRepository
                .findByEmail(email)
                .orElseThrow(() ->
                        new UsernameNotFoundException(
                                "Client introuvable avec l'email : " + email
                        )
                );

        return User.builder()
                .username(client.getEmail())
                .password(client.getMot_de_passe())
                .authorities(
                        List.of(
                                new SimpleGrantedAuthority("ROLE_CLIENT")
                        )
                )
                .build();
    }
}
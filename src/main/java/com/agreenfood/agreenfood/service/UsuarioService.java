package com.agreenfood.agreenfood.service;

import com.agreenfood.agreenfood.model.Usuario;
import com.agreenfood.agreenfood.model.UsuarioLogin;
import com.agreenfood.agreenfood.repository.UsuarioRepository;
import com.agreenfood.agreenfood.security.JwtService;
import com.agreenfood.agreenfood.security.UserDetailsImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import java.util.Optional;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@Service
public class UsuarioService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private AuthenticationManager authenticationManager;

    public Optional<Usuario> cadastrarUsuario(Usuario usuario) {

        if (usuarioRepository.findByUsuario(usuario.getUsuario()).isPresent())
            return Optional.empty();

        usuario.setSenha(criptografarSenha(usuario.getSenha()));

        return Optional.of(usuarioRepository.save(usuario));
    }


    public Optional<Usuario> atualizarUsuario(Usuario usuario){
        if(usuarioRepository.findById(usuario.getId()).isPresent()) {
            Optional<Usuario> buscaUsuario = usuarioRepository.findByUsuario(usuario.getUsuario());
            if ( (buscaUsuario.isPresent()) && ( buscaUsuario.get().getId() != usuario.getId()))
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Usuário já existe!", null);

            usuario.setSenha(criptografarSenha(usuario.getSenha()));
            return Optional.ofNullable(usuarioRepository.save(usuario));
        }
        return Optional.empty();
    }

    public Optional<UsuarioLogin> autenticarUsuario(Optional<UsuarioLogin> usuarioLogin) {

        var credenciais = new UsernamePasswordAuthenticationToken(usuarioLogin.get().getUsuario(),
                usuarioLogin.get().getSenha());

        Authentication authentication = authenticationManager.authenticate(credenciais);

        if (authentication.isAuthenticated()) {
            Optional<Usuario> usuario = usuarioRepository.findByUsuario(usuarioLogin.get().getUsuario());

            if (usuario.isPresent()) {
                usuarioLogin.get().setId(usuario.get().getId());
                usuarioLogin.get().setNome(usuario.get().getNomeCompleto());
                usuarioLogin.get().setUsuario(usuario.get().getUsuario());
                usuarioLogin.get().setToken(gerarToken(usuarioLogin.get().getUsuario()));
                usuarioLogin.get().setSenha("");
                
                return usuarioLogin;
            }
        }
        return Optional.empty();
    }
    private String criptografarSenha(String senha) {

        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

        return encoder.encode(senha);

    }

    private String gerarToken(String usuario) {
        return "Bearer " + jwtService.generateToken(usuario);
    }


}

package am.ejob.backend.api.security;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotNull;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class JwtAuthenticationRequest {

    @NotNull
    @Email
    private String username;
    private String password;
}

package com.project.roomloop.entity;


import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "app_user")
public class User implements UserDetails{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "Mobile Number is required")
    @Size(min = 10,max = 10, message = "Mobile number should be in 10 digit only")
    @Pattern(regexp = "^[0-9]{10}$",message = "in Mobile number only number are allowed")
    @Column(unique = true,nullable = false)
    private String mobileNumber;

    private String name;

    @NotNull(message = "Email is required")
    @Email(message = "enter a valid email")
    @Column(unique = true)
    private String email;

    @NotBlank(message = "Password is required and more that 8 characters")
    @Size(min = 8, max = 200, message = "Password must be between 8 and 20 characters") // keeping this 200 only for now cause insterig manualy sql hash password issue
    @Pattern(
            regexp = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[@$!%*?&]).+$",
            message = "Password must have a letter, number and special character"
    )
    @Column(nullable = false)
    private String password;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of();
    }

    @Override
    public String getUsername() {
        return email;
    }
}

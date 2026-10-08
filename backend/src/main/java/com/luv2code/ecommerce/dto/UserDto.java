package com.luv2code.ecommerce.dto;

import com.luv2code.ecommerce.entity.User;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UserDto {

    private Long id;
    private String email;
    private String firstName;
    private String lastName;
    private String role;

    public static UserDto from(User user) {
        return new UserDto(user.getId(), user.getEmail(), user.getFirstName(),
                user.getLastName(), user.getRole().name());
    }
}

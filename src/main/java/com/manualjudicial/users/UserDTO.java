package com.manualjudicial.users;

import lombok.Data;

@Data
public class UserDTO {
    private String name;
    private String email;
    private String password;
    private Role role;
    private AccessType accessType;
}

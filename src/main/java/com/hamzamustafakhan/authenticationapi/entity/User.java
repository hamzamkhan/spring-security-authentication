package com.hamzamustafakhan.authenticationapi.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.util.Date;

@Entity
@Table(name = "user")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column
    private int id;
    @Column
    @Email(message = "The provided string doesn't follow the email pattern")
    @NotBlank(message = "Email can't be blank")
    private String email;
    @Column
    @Size(min = 6, message = "Password should min 6 characters")
    private String password;
    @Column
    @NotBlank(message = "Name cannot be blank")
    private String name;
    @Column(name = "failed_attempts")
    private int failedAttempts;
    @Column(name = "created_at",
    columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    @Temporal(TemporalType.TIMESTAMP)
    private Date createdAt;
    @Column
    private String status;
    @Column(name = "updated_at",
            columnDefinition = "TIMESTAMP DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP")
    @Temporal(TemporalType.TIMESTAMP)
    private Date updatedAt;
    @Column
    private int role;
}

package com.rmh.auth.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import java.util.Set;
import java.util.HashSet;

@Entity
@Table(name = "users", indexes = {
    @Index(name = "idx_users_username", columnList = "username")
})
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(nullable=false, unique=true, length=100)
    private String username;

    @NotBlank
    @Column(nullable=false)
    private String password;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "user_roles", joinColumns = @JoinColumn(name="user_id"))
    @Column(name="role")
    private Set<String> roles = new HashSet<>();

    // Constructors
    public User(){}

    // Getters/Setters
    public Long getId(){ return id; }
    public void setId(Long id){ this.id = id; }
    public String getUsername(){ return username; }
    public void setUsername(String username){ this.username = username; }
    public String getPassword(){ return password; }
    public void setPassword(String password){ this.password = password; }
    public Set<String> getRoles(){ return roles; }
    public void setRoles(Set<String> roles){ this.roles = roles; }
}

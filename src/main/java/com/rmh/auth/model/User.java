package com.rmh.auth.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import java.util.Set;
import java.util.HashSet;

@Entity
@Table(name = "users", indexes = {
    @Index(name = "idx_users_email", columnList = "email")
})
public class User extends BaseAuditableEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(nullable=false, unique=true, length=150)
    private String email;

    @NotBlank
    @Column(nullable=false, length=50)
    private String name;

    @NotBlank
    @Column(nullable=false)
    private String password;

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private Address address;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "user_roles", joinColumns = @JoinColumn(name="user_id"))
    @Column(name="role")
    private Set<String> roles = new HashSet<>();

    // Constructors
    public User(){}

    // Getters/Setters
    public Long getId(){ return id; }
    public void setId(Long id){ this.id = id; }
    public String getEmail(){ return email; }
    public void setEmail(String email){ this.email = email; }
    public String getName(){ return name; }
    public void setName(String name){ this.name = name; }
    public String getPassword(){ return password; }
    public void setPassword(String password){ this.password = password; }
    public Address getAddress() { return address; }
    public void setAddress(Address address) { this.address = address; }
    public Set<String> getRoles(){ return roles; }
    public void setRoles(Set<String> roles){ this.roles = roles; }
}

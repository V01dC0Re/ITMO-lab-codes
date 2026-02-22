package com.example.app.entity;

import jakarta.persistence.*;
import java.io.Serializable;

@Entity
@Table(name = "users")
public class User implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(unique = true, nullable = false, length = 50)
    private String login;
    
    @Column(nullable = false, length = 100)
    private String passwordHash;
    
    @Column(nullable = false, length = 50)
    private String fullName;
    
    @Column(nullable = false, length = 20)
    private String groupName;
    
    @Column(nullable = false, length = 10)
    private String variantNumber;
    
    // Геттеры и сеттеры
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getLogin() { return login; }
    public void setLogin(String login) { this.login = login; }
    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public String getGroupName() { return groupName; }
    public void setGroupName(String groupName) { this.groupName = groupName; }
    public String getVariantNumber() { return variantNumber; }
    public void setVariantNumber(String variantNumber) { this.variantNumber = variantNumber; }
}

package com.example.demo.entity;

import lombok.Getter;
import lombok.Setter;
import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Entity
@Table(
        name = "customer",
        indexes = {
                @Index(name = "idx_customer_username", columnList = "username")
        }
)
public class Customer extends Base {

    @Column(name = "fullname", length = 255)
    private String fullname;

    @Column(name = "phone", length = 50)
    private String phone;

    @Column(name = "email", length = 255)
    private String email;

    @Column(name = "username", unique = true, nullable = false, length = 100)
    private String username;

    @Column(name = "password", nullable = false, length = 255)
    private String password;

    @Column(name = "companyname", length = 255)
    private String companyname;

    @Column(name = "demand", length = 255)
    private String demand;

    @Column(name = "status", length = 100)
    private String status;

    @Column(name = "is_active")
    private Integer isActive = 1;

    @OneToMany(mappedBy = "customer", fetch = FetchType.LAZY)
    private List<Transaction> transactionEntities = new ArrayList<>();

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "assignmentcustomer",
            joinColumns = @JoinColumn(name = "customerid", nullable = false),
            inverseJoinColumns = @JoinColumn(name = "staffid", nullable = false)
    )
    private List<User> userEntities = new ArrayList<>();
}

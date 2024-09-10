package com.bank.accounts.repository;

import com.bank.accounts.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {
    Optional<Customer> findByMobileNumber(String mobileNumber); //derivative classType JPA will make sure to find
                                                               // bY column nameMobileNumber even if it's not primary key

}

package com.bank.accounts.service.impl;

import com.bank.accounts.dto.AccountsDto;
import com.bank.accounts.dto.CardsDto;
import com.bank.accounts.dto.CustomerDetailsDto;
import com.bank.accounts.dto.LoansDto;
import com.bank.accounts.entity.Accounts;
import com.bank.accounts.entity.Customer;
import com.bank.accounts.exception.ResourceNotFoundException;
import com.bank.accounts.mapper.AccountsMapper;
import com.bank.accounts.mapper.CustomerMapper;
import com.bank.accounts.repository.AccountsRepository;
import com.bank.accounts.repository.CustomerRepository;
import com.bank.accounts.service.ICustomersService;
import com.bank.accounts.service.client.CardsFeignClient;
import com.bank.accounts.service.client.LoansFeignClient;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class CustomersServiceImpl implements ICustomersService {

    private AccountsRepository accountsRepository;
    private CustomerRepository customerRepository;
    @Qualifier("com.bank.accounts.service.client.CardsFeignClient")
    private CardsFeignClient cardsFeignClient;
    @Qualifier("com.bank.accounts.service.client.LoansFeignClient")
    private LoansFeignClient loansFeignClient;


    /**
     * @param mobileNumber - Input Mobile Number
     * @return Customer Details based on a given mobileNumber
     */
    @Override
    public CustomerDetailsDto fetchCustomerDetails(String mobileNumber, String correlationId) {

        Customer customer = customerRepository.findByMobileNumber(mobileNumber).orElseThrow(
                () -> new ResourceNotFoundException("Customer", "mobileNumber", mobileNumber)
        );
        Accounts accounts = accountsRepository.findByCustomerId(customer.getCustomerId()).orElseThrow(
                () -> new ResourceNotFoundException("Account", "customerId", customer.getCustomerId().toString())
        );
        CustomerDetailsDto customerDetailsDto = CustomerMapper.mapToCustomerDetailsDto(customer, new CustomerDetailsDto());
        customerDetailsDto.setAccountsDto(AccountsMapper.mapToAccountsDto(accounts, new AccountsDto()));

        //now setting cards,loans in customerDetailsDto from fetching using feign client
        ResponseEntity<CardsDto> cardsDtoResponseEntity=cardsFeignClient.fetchCardDetails(correlationId, mobileNumber);
        ResponseEntity<LoansDto>loansDtoResponseEntity=loansFeignClient.fetchLoanDetails(correlationId, mobileNumber);

        if(cardsDtoResponseEntity!=null)customerDetailsDto.setCardsDto(cardsDtoResponseEntity.getBody());
        if(loansDtoResponseEntity!=null)customerDetailsDto.setLoansDto(loansDtoResponseEntity.getBody());

        return customerDetailsDto;

    }
}

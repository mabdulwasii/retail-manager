package com.princely.shopmanager.sales.service;

import com.princely.shopmanager.sales.domain.SalesTransaction;
import com.princely.shopmanager.sales.repository.SalesTransactionRepository;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SalesTransactionService {

    private final SalesTransactionRepository salesTransactionRepository;

    SalesTransaction findById(String id) {
        return salesTransactionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Transaction not found: " + id));
    }

}

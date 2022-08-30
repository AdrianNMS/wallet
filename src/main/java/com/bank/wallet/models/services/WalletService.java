package com.bank.wallet.models.services;

import com.bank.wallet.models.documents.Wallet;
import reactor.core.publisher.Mono;

import java.util.List;

public interface WalletService
{
    Mono<List<Wallet>> findAll();
    Mono<Wallet> find(String id);
    Mono<Wallet> create(Wallet wallet);
    Mono<Wallet> update(String id, Wallet wallet);
    Mono<Object> delete(String id);
}

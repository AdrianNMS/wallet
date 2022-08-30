package com.bank.wallet.models.dao;

import com.bank.wallet.models.documents.Wallet;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

public interface WalletDao extends ReactiveMongoRepository<Wallet,String>
{
}

package com.bank.wallet.models.services.impl;

import com.bank.wallet.models.dao.WalletDao;
import com.bank.wallet.models.documents.Wallet;
import com.bank.wallet.models.services.WalletService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.List;

@Service
public class WalletImpl implements WalletService
{
    @Autowired
    private WalletDao dao;

    @Autowired
    private ReactiveRedisTemplate<String, Wallet> redisTemplate;
    @Override
    public Mono<List<Wallet>> findAll() {
        return dao.findAll()
                .collectList();
    }

    @Override
    public Mono<Wallet> find(String id) {
        return redisTemplate.opsForValue().get(id)
                .switchIfEmpty(dao.findById(id)
                        .doOnNext(wal -> redisTemplate.opsForValue()
                                .set(wal.getId(), wal)
                                .subscribe(aBoolean -> {
                                    redisTemplate.expire(id, Duration.ofMinutes(10)).subscribe();
                                })));
    }

    @Override
    public Mono<Wallet> create(Wallet wallet) {
        return dao.save(wallet)
                .doOnNext(wal -> redisTemplate.opsForValue()
                        .set(wal.getId(), wal)
                        .subscribe(aBoolean -> {
                            redisTemplate.expire(wal.getId(), Duration.ofMinutes(10)).subscribe();
                        }));
    }

    @Override
    public Mono<Wallet> update(String id, Wallet wallet) {
        return dao.existsById(id).flatMap(check ->
        {
            if (Boolean.TRUE.equals(check))
            {
                redisTemplate.opsForValue().delete(id).subscribe();
                return dao.save(wallet)
                        .doOnNext(wal -> redisTemplate.opsForValue()
                                .set(wal.getId(), wal)
                                .subscribe(aBoolean -> {
                                    redisTemplate.expire(id, Duration.ofMinutes(10));
                                }));
            }
            else
                return Mono.empty();

        });
    }

    @Override
    public Mono<Object> delete(String id) {
        return dao.existsById(id).flatMap(check -> {
            if (Boolean.TRUE.equals(check))
            {
                redisTemplate.opsForValue().delete(id).subscribe();
                return dao.deleteById(id).then(Mono.just(true));
            }
            else
                return Mono.empty();
        });
    }
}

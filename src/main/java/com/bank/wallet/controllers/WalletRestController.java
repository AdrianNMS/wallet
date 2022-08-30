package com.bank.wallet.controllers;

import com.bank.wallet.handler.ResponseHandler;
import com.bank.wallet.models.documents.Wallet;
import com.bank.wallet.models.services.WalletService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/wallet")
public class WalletRestController
{
    @Autowired
    private WalletService walletService;

    private static final Logger log = LoggerFactory.getLogger(WalletRestController.class);

    @PostMapping
    public Mono<ResponseEntity<Object>> create(@Validated @RequestBody Wallet wal) {
        return walletService.create(wal)
                .doOnNext(wallet -> log.info(wallet.toString()))
                .flatMap(wallet -> Mono.just(ResponseHandler.response("Done", HttpStatus.OK, wallet)))
                .onErrorResume(error -> Mono.just(ResponseHandler.response(error.getMessage(), HttpStatus.BAD_REQUEST, null)));
    }

    @GetMapping
    public Mono<ResponseEntity<Object>> findAll() {
        return walletService.findAll()
                .doOnNext(wallets -> log.info(wallets.toString()))
                .flatMap(wallets -> Mono.just(ResponseHandler.response("Done", HttpStatus.OK, wallets)))
                .onErrorResume(error -> Mono.just(ResponseHandler.response(error.getMessage(), HttpStatus.BAD_REQUEST, null)));

    }

    @GetMapping("/{id}")
    public Mono<ResponseEntity<Object>> find(@PathVariable String id) {
        return walletService.find(id)
                .doOnNext(wallet -> log.info(wallet.toString()))
                .map(wallet -> ResponseHandler.response("Done", HttpStatus.OK, wallet))
                .onErrorResume(error -> Mono.just(ResponseHandler.response(error.getMessage(), HttpStatus.BAD_REQUEST, null)));
    }

    @PutMapping("/{id}")
    public Mono<ResponseEntity<Object>> update(@PathVariable("id") String id,@Validated @RequestBody Wallet wal) {
        return walletService.update(id,wal)
                .flatMap(wallet -> Mono.just(ResponseHandler.response("Done", HttpStatus.OK, wallet)))
                .onErrorResume(error -> Mono.just(ResponseHandler.response(error.getMessage(), HttpStatus.BAD_REQUEST, null)))
                .switchIfEmpty(Mono.just(ResponseHandler.response("Empty", HttpStatus.NO_CONTENT, null)));
    }

    @DeleteMapping("/{id}")
    public Mono<ResponseEntity<Object>> delete(@PathVariable("id") String id) {
        return walletService.delete(id)
                .flatMap(o -> Mono.just(ResponseHandler.response("Done", HttpStatus.OK, null)))
                .onErrorResume(error -> Mono.just(ResponseHandler.response(error.getMessage(), HttpStatus.BAD_REQUEST, null)))
                .switchIfEmpty(Mono.just(ResponseHandler.response("Error", HttpStatus.NO_CONTENT, null)));
    }
}
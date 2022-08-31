package com.bank.wallet.controllers;

import com.bank.wallet.handler.ResponseHandler;
import com.bank.wallet.models.documents.Wallet;
import com.bank.wallet.models.enums.TransferenceType;
import com.bank.wallet.models.kafka.RequestWallet;
import com.bank.wallet.models.kafka.ResponseTransference;
import com.bank.wallet.models.services.WalletService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/wallet")
public class WalletRestController
{
    @Autowired
    private WalletService walletService;

    @Autowired
    private KafkaTemplate<String, ResponseTransference> template;

    private static final Logger log = LoggerFactory.getLogger(WalletRestController.class);

    @PostMapping
    public Mono<ResponseEntity<Object>> create(@Validated @RequestBody Wallet wal) {
        log.info("[INI] create");

        return walletService.create(wal)
                .doOnNext(wallet -> log.info(wallet.toString()))
                .flatMap(wallet -> Mono.just(ResponseHandler.response("Done", HttpStatus.OK, wallet)))
                .onErrorResume(error -> Mono.just(ResponseHandler.response(error.getMessage(), HttpStatus.BAD_REQUEST, null)))
                .doFinally(fin -> log.info("[END] create"));
    }

    @GetMapping
    public Mono<ResponseEntity<Object>> findAll() {
        log.info("[INI] findAll");

        return walletService.findAll()
                .doOnNext(wallets -> log.info(wallets.toString()))
                .flatMap(wallets -> Mono.just(ResponseHandler.response("Done", HttpStatus.OK, wallets)))
                .onErrorResume(error -> Mono.just(ResponseHandler.response(error.getMessage(), HttpStatus.BAD_REQUEST, null)))
                .doFinally(fin -> log.info("[END] findAll"));

    }

    @GetMapping("/{id}")
    public Mono<ResponseEntity<Object>> find(@PathVariable String id) {
        log.info("[INI] find");

        return walletService.find(id)
                .doOnNext(wallet -> log.info(wallet.toString()))
                .map(wallet -> ResponseHandler.response("Done", HttpStatus.OK, wallet))
                .onErrorResume(error -> Mono.just(ResponseHandler.response(error.getMessage(), HttpStatus.BAD_REQUEST, null)))
                .doFinally(fin -> log.info("[END] find"));
    }

    @PutMapping("/{id}")
    public Mono<ResponseEntity<Object>> update(@PathVariable("id") String id,@Validated @RequestBody Wallet wal) {
        log.info("[INI] update");

        return walletService.update(id,wal)
                .flatMap(wallet -> Mono.just(ResponseHandler.response("Done", HttpStatus.OK, wallet)))
                .onErrorResume(error -> Mono.just(ResponseHandler.response(error.getMessage(), HttpStatus.BAD_REQUEST, null)))
                .switchIfEmpty(Mono.just(ResponseHandler.response("Empty", HttpStatus.NO_CONTENT, null)))
                .doFinally(fin -> log.info("[END] update"));
    }

    @DeleteMapping("/{id}")
    public Mono<ResponseEntity<Object>> delete(@PathVariable("id") String id) {
        log.info("[INI] delete");

        return walletService.delete(id)
                .flatMap(o -> Mono.just(ResponseHandler.response("Done", HttpStatus.OK, null)))
                .onErrorResume(error -> Mono.just(ResponseHandler.response(error.getMessage(), HttpStatus.BAD_REQUEST, null)))
                .switchIfEmpty(Mono.just(ResponseHandler.response("Error", HttpStatus.NO_CONTENT, null)))
                .doFinally(fin -> log.info("[END] delete"));
    }

    @KafkaListener(topics = "wallet-check", groupId = "wallet")
    public void receiveCheckBootcoins(@Payload RequestWallet requestWallet)
    {
        log.info("[INI] receiveCheckBootcoins");

        var userCheck = (requestWallet.getTransferenceType() == TransferenceType.BUY)
                ? requestWallet.getIdReceiver() : requestWallet.getIdSender();

        log.info(userCheck);

        walletService.find(userCheck).subscribe(wallet -> {

            var status = (wallet!=null && wallet.getBootcoins()>=requestWallet.getBootcoins());

            var response = ResponseTransference.builder()
                    .idTransference(requestWallet.getIdTransference())
                    .status(status)
                    .build();

            log.info(response.toString());

            template.send("transference_wallet-check",response);

            log.info("[END] receiveCheckBootcoins");

        });
    }

    @KafkaListener(topics = "wallet-update", groupId = "wallet")
    public void receiveUpdateBootcoins(@Payload RequestWallet requestWallet)
    {
        log.info("[INI] receiveUpdateBootcoins");

        var user1Check = (requestWallet.getTransferenceType() == TransferenceType.BUY)
                ? requestWallet.getIdReceiver() : requestWallet.getIdSender();
        var user2Check = (requestWallet.getTransferenceType() == TransferenceType.BUY)
                ? requestWallet.getIdSender() : requestWallet.getIdReceiver();

        log.info(user1Check);
        log.info(user2Check);

        walletService.updateBootCoins(user1Check, requestWallet.getBootcoins())
                .subscribe(wallet -> {

                    log.info(wallet.toString());

                    if(wallet!=null)
                        walletService.updateBootCoins(user2Check, -requestWallet.getBootcoins())
                                .subscribe(wallet1 -> {

                                    log.info(wallet1.toString());

                                    template.send("transference_wallet-update",ResponseTransference.builder()
                                            .idTransference(requestWallet.getIdTransference())
                                            .status((wallet1!=null))
                                            .build());
                                });
                    else
                        template.send("transference_wallet-update",ResponseTransference.builder()
                                .idTransference(requestWallet.getIdTransference())
                                .status(false)
                                .build());
                    log.info("[END] receiveUpdateBootcoins");
                });
    }
}

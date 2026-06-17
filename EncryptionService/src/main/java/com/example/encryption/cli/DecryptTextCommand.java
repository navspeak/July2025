package com.example.encryption.cli;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Component
@Profile("cli")
@Command(name = "decrypt-text", description = "Decrypt a vault:v1:... ciphertext, prints plaintext to stdout")
public class DecryptTextCommand implements Runnable {

    @Option(names = "--ciphertext", required = true, description = "Vault ciphertext token (vault:v1:...)")
    private String ciphertext;

    @Option(names = "--transit-key", description = "Vault transit key name")
    private String transitKey;

    private final EncryptionClient client;

    public DecryptTextCommand(EncryptionClient client) {
        this.client = client;
    }

    @Override
    public void run() {
        System.out.println(client.decryptText(ciphertext, transitKey));
    }
}

package com.example.encryption.cli;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Component
@Profile("cli")
@Command(name = "encrypt-text", mixinStandardHelpOptions = true, description = "Encrypt a plaintext string, prints ciphertext to stdout")
public class EncryptTextCommand implements Runnable {

    @Option(names = "--plaintext", required = true, description = "Text to encrypt")
    private String plaintext;

    @Option(names = "--transit-key", description = "Vault transit key name")
    private String transitKey;

    private final EncryptionClient client;

    public EncryptTextCommand(EncryptionClient client) {
        this.client = client;
    }

    @Override
    public void run() {
        System.out.println(client.encryptText(plaintext, transitKey));
    }
}

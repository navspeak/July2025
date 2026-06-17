package com.example.encryption.cli;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.ExitCodeGenerator;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Profile;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Spec;

@SpringBootApplication(scanBasePackages = "com.example.encryption")
@EnableConfigurationProperties(CliProperties.class)
@Profile("cli")
public class EncryptionCliApplication implements CommandLineRunner, ExitCodeGenerator {

    private final EncryptDirCommand encryptDirCommand;
    private final EncryptZipCommand encryptZipCommand;
    private final EncryptTextCommand encryptTextCommand;
    private int exitCode;

    public EncryptionCliApplication(EncryptDirCommand encryptDirCommand,
                                    EncryptZipCommand encryptZipCommand,
                                    EncryptTextCommand encryptTextCommand) {
        this.encryptDirCommand = encryptDirCommand;
        this.encryptZipCommand = encryptZipCommand;
        this.encryptTextCommand = encryptTextCommand;
    }

    public static void main(String[] args) {
        System.setProperty("spring.profiles.active", "cli");
        System.exit(SpringApplication.exit(SpringApplication.run(EncryptionCliApplication.class, args)));
    }

    @Override
    public void run(String... args) {
        exitCode = new CommandLine(new RootCommand())
                .addSubcommand(encryptDirCommand)
                .addSubcommand(encryptZipCommand)
                .addSubcommand(encryptTextCommand)
                .execute(args);
    }

    @Override
    public int getExitCode() { return exitCode; }

    @Command(name = "enc", mixinStandardHelpOptions = true,
             description = "Encryption CLI — batch encrypt files or text via the Encryption Service")
    static class RootCommand implements Runnable {
        @Spec CommandSpec spec;

        @Override
        public void run() {
            spec.commandLine().usage(System.out);
        }
    }
}

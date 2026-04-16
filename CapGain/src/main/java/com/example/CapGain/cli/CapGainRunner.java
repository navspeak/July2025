package com.example.CapGain.cli;

import com.example.CapGain.processor.ParsingProcessor;
import com.example.CapGain.util.TransactionReadUtil;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import java.util.List;

@Component
@ConditionalOnProperty(
        prefix = "app",
        name = "cli-enabled",
        havingValue = "true",
        matchIfMissing = true
)
@Slf4j
@AllArgsConstructor
public class CapGainRunner implements CommandLineRunner {
    private final ParsingProcessor parsingProcessor;

    @Override
    public void run(String... args) throws Exception {
        List<String> lines = TransactionReadUtil.readFromConsole();
        String result = parsingProcessor.process(lines);
        System.out.println(result);

    }

}

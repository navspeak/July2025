package com.example.CapGain.processor;

import com.example.CapGain.model.Tax;
import com.example.CapGain.model.Transaction;
import com.example.CapGain.service.TaxCalculatorService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.UncheckedIOException;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Component
@AllArgsConstructor
public class ParsingProcessor {
    private final ObjectMapper objectMapper;
    private final TaxCalculatorService taxCalculatorService;

    public String process(List<String> lines) {
        var listOfTransactions = lines.stream()
                .map(this::mapToTransaction)
                .toList();

        var listOfTax = listOfTransactions.stream()
                .map(taxCalculatorService::computeTax)
                .toList();

        return listOfTax.stream()
                .map(this::mapToString)
                .map(String::trim)
                .collect(Collectors.joining(System.lineSeparator()));

    }

    private List<Transaction> mapToTransaction(String line){
        try {
            return objectMapper.readValue(line, new TypeReference<>() {});
        } catch (JsonProcessingException e) {
            log.error("Error while reading input line ", e);
            throw new UncheckedIOException(e);
        }
    }

    private String mapToString_(List<Tax> taxList){
        try {
            // Let Jackson handle the List to get proper brackets and commas
            return objectMapper.writeValueAsString(taxList);
        } catch (JsonProcessingException e) {
            log.error("Error while writing Tax list as string ", e);
            throw new UncheckedIOException(e);
        }

    }

    private String mapToString(List<Tax> taxList){
        try {
            return objectMapper.writeValueAsString(taxList);
        } catch (JsonProcessingException e) {
            log.error("Error while writing Tax as string ", e);
            throw new UncheckedIOException(e);
        }
    }

    private String writeValueAsString(Tax tax) {
        try {
            return objectMapper.writeValueAsString(tax);
        } catch (JsonProcessingException e) {
            log.error("Error while writing Tax as string ", e);
            throw new UncheckedIOException(e);
        }
    }


}

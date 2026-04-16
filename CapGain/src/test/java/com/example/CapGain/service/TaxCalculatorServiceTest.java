package com.example.CapGain.service;

import com.example.CapGain.processor.ParsingProcessor;
import com.example.CapGain.util.TransactionReadUtil;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
class TaxCalculatorServiceTest {

    ObjectMapper objectMapper = new ObjectMapper();
    TaxCalculatorService taxCalculatorService = new TaxCalculatorService();
    ParsingProcessor parsingProcessor ;

    @BeforeEach
    void setup(){
//        objectMapper.enable(JsonGenerator.Feature.WRITE_BIGDECIMAL_AS_PLAIN);
        parsingProcessor = new ParsingProcessor(objectMapper, taxCalculatorService);
    }


    @Test
    void testCase1(){
        String input = """
            [{"operation":"buy", "unit-cost":10.00, "quantity": 100},{"operation":"sell", "unit-cost":15.00, "quantity": 50},{"operation":"sell", "unit-cost":15.00, "quantity": 50}]
            """;
        String output = """
                [{"tax": 0.0},{"tax": 0.0},{"tax": 0.0}]
                """;
        List<String> lines = TransactionReadUtil.readFromString(input);
        var result = parsingProcessor.process(lines);
        assertEquals(normalize(output), normalize(result));
    }

    @Test
    void testCase2(){
        String input = """
                [{"operation":"buy", "unit-cost":10.00, "quantity": 10000},
                {"operation":"sell", "unit-cost":20.00, "quantity": 5000},
                {"operation":"sell", "unit-cost":5.00, "quantity": 5000}]
            """.replace("\r","").replace("\n", "");
        String output = """
                [{"tax": 0.0},{"tax": 10000.0},{"tax": 0.0}]
                """.replace("\r","").replace("\n", "");
        List<String> lines = TransactionReadUtil.readFromString(input);
        var result = parsingProcessor.process(lines);
        assertEquals(normalize(output), normalize(result));
    }

    @Test
    void testCase1_2(){
        String input = """
            [{"operation":"buy", "unit-cost":10.00, "quantity": 100},{"operation":"sell", "unit-cost":15.00, "quantity": 50},{"operation":"sell", "unit-cost":15.00, "quantity": 50}]
            [{"operation":"buy", "unit-cost":10.00, "quantity": 10000},{"operation":"sell", "unit-cost":20.00, "quantity": 5000},{"operation":"sell", "unit-cost":5.00, "quantity": 5000}]
            """;
        String output = """
                [{"tax": 0.0},{"tax": 0.0},{"tax": 0.0}]
                [{"tax": 0.0},{"tax": 10000.0},{"tax": 0.0}]
                """;
        List<String> lines = TransactionReadUtil.readFromString(input);
        var result = parsingProcessor.process(lines);
        assertEquals(normalize(output), normalize(result));
    }

    @Test
    void testCase3(){
        String input = """
                [{"operation":"buy", "unit-cost":10.00, "quantity": 10000},
                {"operation":"sell", "unit-cost":5.00, "quantity": 5000},
                {"operation":"sell", "unit-cost":20.00, "quantity": 3000}]
            """.replace("\r","").replace("\n", "");;
        String output = """
                    [{"tax": 0.0},{"tax": 0.0},{"tax": 1000.0}]
                """.replace("\r","").replace("\n", "");;
        List<String> lines = TransactionReadUtil.readFromString(input);
        var result = parsingProcessor.process(lines);
        assertEquals(normalize(output), normalize(result));
    }

    @Test
    void testCase4(){
        String input = """
                [{"operation":"buy", "unit-cost":10.00, "quantity": 10000},
                {"operation":"buy", "unit-cost":25.00, "quantity": 5000},
                {"operation":"sell", "unit-cost":15.00, "quantity": 10000}]
            """.replace("\r","").replace("\n", "");;
        String output = """
                [{"tax": 0.0},{"tax": 0.0},{"tax": 0.0}]
                """.replace("\r","").replace("\n", "");;
        List<String> lines = TransactionReadUtil.readFromString(input);
        var result = parsingProcessor.process(lines);
        assertEquals(normalize(output), normalize(result));
    }


    @Test
    void testCase5(){
        String input = """
                [{"operation":"buy", "unit-cost":10.00, "quantity": 10000},
                {"operation":"buy", "unit-cost":25.00, "quantity": 5000},
                {"operation":"sell", "unit-cost":15.00, "quantity": 10000},
                {"operation":"sell", "unit-cost":25.00, "quantity": 5000}]
            """.replace("\r","").replace("\n", "");;
        String output = """
                [{"tax": 0.0},{"tax": 0.0},{"tax": 0.0},{"tax": 10000.0}]
                """.replace("\r","").replace("\n", "");;
        List<String> lines = TransactionReadUtil.readFromString(input);
        var result = parsingProcessor.process(lines);
        assertEquals(normalize(output), normalize(result));
    }

    @Test
    void testCase6(){
        String input = """
                [{"operation":"buy", "unit-cost":10.00, "quantity": 10000},
                {"operation":"sell", "unit-cost":2.00, "quantity": 5000},
                {"operation":"sell", "unit-cost":20.00, "quantity": 2000},
                {"operation":"sell", "unit-cost":20.00, "quantity": 2000},
                {"operation":"sell", "unit-cost":25.00, "quantity": 1000}]
            """.replace("\r","").replace("\n", "");;
        String output = """
               [{"tax": 0.0},{"tax": 0.0},{"tax": 0.0},{"tax": 0.0},{"tax": 3000.0}]
                """.replace("\r","").replace("\n", "");;
        List<String> lines = TransactionReadUtil.readFromString(input);
        var result = parsingProcessor.process(lines);
        assertEquals(normalize(output), normalize(result));
    }

    @Test
    void testCase7(){
        String input = """
                [{"operation":"buy", "unit-cost":10.00, "quantity": 10000},
                {"operation":"sell", "unit-cost":2.00, "quantity": 5000},
                {"operation":"sell", "unit-cost":20.00, "quantity": 2000},
                {"operation":"sell", "unit-cost":20.00, "quantity": 2000},
                {"operation":"sell", "unit-cost":25.00, "quantity": 1000},
                {"operation":"buy", "unit-cost":20.00, "quantity": 10000},
                {"operation":"sell", "unit-cost":15.00, "quantity": 5000},
                {"operation":"sell", "unit-cost":30.00, "quantity": 4350},
                {"operation":"sell", "unit-cost":30.00, "quantity": 650}]
            """.replace("\r","").replace("\n", "");
        String output = """
                [{"tax": 0.0},{"tax": 0.0},{"tax": 0.0},{"tax": 0.0},{"tax": 3000.0},\
                {"tax": 0.0},{"tax": 0.0},{"tax": 3700.0},{"tax": 0.0}]
                """.replace("\r","").replace("\n", "");
        List<String> lines = TransactionReadUtil.readFromString(input);
        var result = parsingProcessor.process(lines);
        assertEquals(normalize(output), normalize(result));
    }
    @Test
    void testCase8(){
        String input = """
                [{"operation":"buy", "unit-cost":10.00, "quantity": 10000},
                {"operation":"sell", "unit-cost":50.00, "quantity": 10000},
                {"operation":"buy", "unit-cost":20.00, "quantity": 10000},
                {"operation":"sell", "unit-cost":50.00, "quantity": 10000}]
            """.replace("\r","").replace("\n", "");
        String output = """
                [{"tax": 0.0},{"tax": 80000.0},{"tax": 0.0},{"tax": 60000.0}]
                """.replace("\r","").replace("\n", "");
        List<String> lines = TransactionReadUtil.readFromString(input);
        var result = parsingProcessor.process(lines);
        assertEquals(normalize(output), normalize(result));
    }


    @Test
    void testCase9(){
        String input = """
                [{"operation":"buy", "unit-cost":  5000.00, "quantity":  10},\s
                    {"operation":"sell", "unit-cost":  4000.00, "quantity":   5},
                    {"operation":"buy",  "unit-cost": 15000.00, "quantity":   5},
                    {"operation":"buy",  "unit-cost":  4000.00, "quantity":   2},
                    {"operation":"buy",  "unit-cost": 23000.00, "quantity":   2},
                    {"operation":"sell", "unit-cost": 20000.00, "quantity":   1},
                    {"operation":"sell", "unit-cost": 12000.00, "quantity":  10},
                    {"operation":"sell", "unit-cost": 15000.00, "quantity":   3}]
            """.replace("\r","").replace("\n", "");
        String output = """
                [{"tax": 0.0},{"tax": 0.0},{"tax": 0.0},{"tax": 0.0},{"tax": 0.0},
                {"tax": 0.0},{"tax": 1000.0},{"tax": 2400.0}]
                """.replace("\r","").replace("\n", "");
        List<String> lines = TransactionReadUtil.readFromString(input);
        var result = parsingProcessor.process(lines);
        assertEquals(normalize(output), normalize(result));
    }



    String normalize(String s){
        return s.replace("\r\n", "\n").replace(" ","").replace("\n","");
    }

}
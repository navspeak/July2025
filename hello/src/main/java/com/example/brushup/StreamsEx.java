package com.example.brushup;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class StreamsEx {
    record Employee(String name, String department, double salary) {}
    public static void main(String[] args) {
        // Lazy so c is not printed
        Stream.of("a", "b", "c", "d", "e")
                .map(x -> { System.out.println("map " + x); return x.toUpperCase(); })
                .filter(x -> { System.out.println("filter " + x); return x.compareTo("B") >= 0; })
                .findFirst();

        List<Integer> list = List.of(1, 2, 3, 4);

        list.stream()
                .peek(x -> System.out.print(x))
                .filter(x -> x > 2);

        List<Employee> employees = Arrays.asList(
                new Employee("Alice", "IT", 90000),
                new Employee("Bob", "IT", 70000),
                new Employee("Charlie", "HR", 60000),
                new Employee("Dan", "HR", 50000),
                new Employee("Eve", "Sales", 80000)
        );

        // Grouping by department and calculating the average salary
        Map<String, Double> avgSalaryByDept = employees.stream()
                .collect(Collectors.groupingBy(
                        Employee::department,                   // Classifier (The Key)
                        Collectors.averagingDouble(Employee::salary) // Downstream Collector (The Value)
                ));

        System.out.println(avgSalaryByDept);
        // Output: {Sales=80000.0, HR=55000.0, IT=80000.0}

        Map<String, Optional<Employee>> topPaidByDept = employees.stream()
                .collect(Collectors.groupingBy(
                        Employee::department,
                        Collectors.maxBy(Comparator.comparingDouble(Employee::salary))
                ));
    }
}

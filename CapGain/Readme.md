# About
This is a command line app which can be run using `capital-gain.sh`

## Building the jar
1. From the root directory run `mvn clean install`
    * the above commands runs all unit tests 
2. This will create `CapGain-0.0.1-SNAPSHOT.jar` in `target` folder

## Running test
- `mvn test` runs thru various cases of buy and sell transactions
- 
## Usage
### Location of scripts and Jar
- The script [capital-gain.sh](capital-gain.sh) is placed in root directory
- The script assumes `CapGain-0.0.1-SNAPSHOT.jar` is placed in the same directory
- If you want to override the Jar location, you can do so by exporting environment variable `JAR_PATH` to point to the jar

## Design
- Spring Boot was chosen for lightweight dependency management and clean component wiring, while configured in non-web mode to preserve CLI behavior.
- 
## Processing
- `ParsingProcessor.process()` is the entry point
- `TransactionReadUtil` has methods to read from console or from a String literal which abstracts the reading of input data
- The logic is written in a fluent pattern enabling focus on business logic
- The tax computation logic is in `TaxCalculatorService`

`
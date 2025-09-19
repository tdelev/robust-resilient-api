# Code for the talk "Robust and Resilient APIs"

This repository contains the code used for the talk "Robust and Resilient APIs" at [WTS 2025](https://wts.sh/sessions/tc0f8nx889d2l8l).

## Requirements

- Java 21
- Gradle

## Running the code

To run the web-api run the following command:

```bash
cd spring/web-api
./gradlew bootRun
```

To run the external-api run the following command:

```bash
cd spring/external-api
./gradlew bootRun
```

To run the load-testing run the following command:

```bash
cd load-testing
./gradlew gatlingRun
```

The load testing will run for around 1 minute and save results in `build/reports/gatling/results`.

## License

This code is licensed under the MIT license.

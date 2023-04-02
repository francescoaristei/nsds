# Apache Spark evaluation lab

Apache Spark evaluation lab for the NSDS course at Politecnico di Milano.

## Problem addressed

Three input datasets

citiesRegion
- Type: static, csv file.
- Fields: city, region

citiesPopulation
- Type: static, csv file.
- Fields: id (of the city), city, population.

bookings
- Type: dynamic, stream.
- Fields: timestamp, value.
- Each entry with value x indicates that someone booked a hotel in the city with id x.

For all queries: limit unnecessary recomputations as much as possible!

- Compute the total population for each region.
- Compute the number of cities and the population of the most populated city for each region.
- Print the evolution of the population in Italy year by year until the total population in Italy overcomes 100M people.


Assume that the population evolves as follows:

- In cities with more than 1000 inhabitants, it increases by 1% every year.
- In cities with less than 1000 inhabitants, it decreased by 1% every year.
 
The output on the terminal should be a sequence of lines.

- Year: 1, total population: xxx.
- Year: 2, total population: yyy.
- …
- You may round the population of each city to the nearest integer during your computation.
- Compute the total number of bookings for each region, in a window of 30 seconds, sliding every 5 seconds.

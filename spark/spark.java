package it.polimi.nsds.spark.eval;

import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.sql.streaming.StreamingQuery;
import org.apache.spark.sql.streaming.StreamingQueryException;
import org.apache.spark.sql.types.DataTypes;
import org.apache.spark.sql.types.StructField;
import org.apache.spark.sql.types.StructType;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeoutException;

import static org.apache.spark.sql.functions.*;

/**
 * Group number: 21
 * Group members:
 * Francesco Aristei 10804304
 * Dario Mazzola 10650009
 * Giampiero Repole 10543357
 */
public class SparkGroup21 {

    final static int threshold = 100000000;

    public static void main(String[] args) throws TimeoutException {
        final String master = args.length > 0 ? args[0] : "local[4]";
        final String filePath = args.length > 1 ? args[1] : "./";

        final SparkSession spark = SparkSession
                .builder()
                .master(master)
                .appName("SparkEval")
                .getOrCreate();
        spark.sparkContext().setLogLevel("ERROR");

        final List<StructField> citiesRegionsFields = new ArrayList<>();
        citiesRegionsFields.add(DataTypes.createStructField("city", DataTypes.StringType, false));
        citiesRegionsFields.add(DataTypes.createStructField("region", DataTypes.StringType, false));
        final StructType citiesRegionsSchema = DataTypes.createStructType(citiesRegionsFields);

        final List<StructField> citiesPopulationFields = new ArrayList<>();
        citiesPopulationFields.add(DataTypes.createStructField("id", DataTypes.IntegerType, false));
        citiesPopulationFields.add(DataTypes.createStructField("city", DataTypes.StringType, false));
        citiesPopulationFields.add(DataTypes.createStructField("population", DataTypes.IntegerType, false));
        final StructType citiesPopulationSchema = DataTypes.createStructType(citiesPopulationFields);

        final Dataset<Row> citiesPopulation = spark
                .read()
                .option("header", "true")
                .option("delimiter", ";")
                .schema(citiesPopulationSchema)
                .csv(filePath + "files/cities_population.csv");

        final Dataset<Row> citiesRegions = spark
                .read()
                .option("header", "true")
                .option("delimiter", ";")
                .schema(citiesRegionsSchema)
                .csv(filePath + "files/cities_regions.csv");

        // Caching citiesPopulation to avoid recomputing the same dataset multiple times.
        // We do not cache citiesRegions because it is used only once in the queries below.
        citiesPopulation.cache();

        final Dataset<Row> populationsPerRegionsAndCities = citiesRegions
                .join(citiesPopulation,
                        citiesRegions.col("city").equalTo(citiesPopulation.col("city")))
                .drop(citiesPopulation.col("city"));

        // Caching populationsPerRegionsAndCities because it is used several times in the queries below
        populationsPerRegionsAndCities.cache();

        final Dataset<Row> q1 = populationsPerRegionsAndCities
                .groupBy(col("region"))
                .sum("population");

        q1.show();

        final Dataset<Row> q2 = populationsPerRegionsAndCities
                .groupBy(col("region"))
                .agg(max("population"), count(col("city")))
                .withColumnRenamed("max(population)", "Most populated city")
                .withColumnRenamed("count(city)", "Number of cities");

        q2.show();

        // JavaRDD where each element is an integer and represents the population of a city
        JavaRDD<Integer> population = citiesPopulation.toJavaRDD().map(r -> r.getInt(2));
        population.cache();

        // Since citiesPopulation will not be used anymore
        // it makes sense to un-persist it from memory
        citiesPopulation.unpersist();

        JavaRDD<Integer> oldPopulation = population;

        int iteration = 0;
        int sum = sumAmount(population);
        while (sum < threshold) {
            population = population.map(i -> {
                if(i > 1000) {
                    i = (int) (i * 1.01);
                }
                else{
                    i = (int) (i * 0.99);
                }

                return i;
            });
            population.cache();
            sum = sumAmount(population);

            System.out.println("Year: " + (++iteration) + ", total population: " + sum);

            oldPopulation.unpersist();
            oldPopulation = population;
        }

        // Bookings: the value represents the city of the booking
        final Dataset<Row> bookings = spark
                .readStream()
                .format("rate")
                .option("rowsPerSecond", 100)
                .load();

        final StreamingQuery q4 = bookings
                .join(populationsPerRegionsAndCities.drop("city"),
                        bookings.col("value").equalTo(populationsPerRegionsAndCities.col("id")))
                .groupBy(col("region"),
                        window(col("timestamp"), "30 seconds", "5 seconds"))
                .count()
                .writeStream()
                .outputMode("update")
                .format("console")
                .start();

        try {
            q4.awaitTermination();
        } catch (final StreamingQueryException e) {
            e.printStackTrace();
        }

        spark.close();
    }

    private static final int sumAmount(JavaRDD<Integer> population) {
        return population
                .reduce(Integer::sum);
    }

}
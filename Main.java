import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;

/**
 * This program computes the following SQL query:
 * select
 * 100 * (sum(price * discount) / sum(price)) as discount_ratio,
 * avg(price) as avg_price
 * from
 * item
 * where (
 * discount between .05 and .07
 * and quantity < 24
 * and status = `A`
 * ) or comment LIKE `PROMO%SUMMER`
 * It is composed by 3 main tasks:
 * 1. Read input
 * 2. Filter by WHERE condition
 * 3. Aggregate
 * Traditionally data is represented in row format:
 * --------------------------------------------------------
 * RowId Quantity Price Discount Status Comment
 * --------------------------------------------------------
 * 0 6 19.9 0.07 "A" "PROMO"
 * --------------------------------------------------------
 * 1 18 24.9 0.04 "A" ""
 * --------------------------------------------------------
 * 2 6 9.9 0.08 "AR" ""
 * --------------------------------------------------------
 * This program represents data in columnar format:
 * -------------------------------------
 * RowId 0 1 2
 * -------------------------------------
 * Quantity 6 18 6
 * -------------------------------------
 * Price 19.9 24.9 9.9
 * -------------------------------------
 * Discount 0.07 0.04 0.08
 * -------------------------------------
 * Status "A" "A" "AR"
 * -------------------------------------

 Sr. Software Engineer Exercise 2
 * Comment "PROMO" "" ""
 * -------------------------------------
 */
public class Main {
    public static class InputBatch {
        public int numRows;
        public int[] quantity;
        public double[] price;
        public double[] discount;
        public StringColumn status = new StringColumn();
        public StringColumn comment = new StringColumn();
    }
    public static class FilteredBatch {
        public int numRows;
        public int[] quantity;
        public double[] price;
        public double[] discount;
        public StringColumn status = new StringColumn();
        public StringColumn comment = new StringColumn();
    }
    public static class AggregatedBatch {
        public int numRows;
        public double[] discount_ratio;
        public double[] avg_price;
    }
    /**
     * Variable length ASCII string.
     */
    public static class StringColumn {
        public int[] offset; // start of string in `buffer` (for each row)
        public int[] length; // lengths (for each row)
        public byte[] buffer; // buffer with data of all strings
        // Example: 2 strings - "Hi" and "there"
        //
        // offset | 0 2
        // length | 2 5
        // buffer | H i t h e r e
    }
    /**
     * Implement TODOs with performance in mind.
     * <br/>
     * Ideally using Java 8 and without calls to JDK libraries.
     * Feel free to create new functions but keep existing ones.
     */
    public static void main(String[] args) {
        InputBatch input = input();
        FilteredBatch filtered = filter(input);
        AggregatedBatch aggregated = aggregate(filtered);
        assertResult(5.8893854d, aggregated.discount_ratio[0], "discount ratio");
        assertResult(17.9d, aggregated.avg_price[0], "avg price");
    }
    public static InputBatch input() {
        InputBatch output = new InputBatch();
        output.numRows = 10;

        //Sr. Software Engineer Exercise 3

        output.quantity = new int[]{ 6, 18, 6, 30, 24, 12, 18, 6, 24, 12 };
        output.price = new double[]{ 19.9d, 24.9d, 9.9d, 14.9d, 9.9d, 19.9d, 24.9d, 19.9d, 9.9d, 14.9 };
        output.discount = inputDiscount();
        output.status.offset = new int[]{ 0, 1, 2, 4, 5, 6, 7, 8, 9, 10 };
        output.status.length = new int[]{ 1, 1, 2, 1, 1, 1, 1, 1, 1, 1 };
        output.status.buffer = "AAARNAANAAA".getBytes();
        output.comment.offset = new int[]{ 0, 0, 0, 5, 0, 0, 0, 0, 20, 0 };
        output.comment.length = new int[]{ 5, 0, 0, 15, 5, 0, 0, 0, 12, 5 };
        output.comment.buffer = "PROMOPROMO IN SUMMERPROMO WINTER".getBytes();
        return output;
    }
    public static double[] inputDiscount() {
// Possible discount values.
        double[] dictionary = { 0.04d, 0.05d, 0.07d, 0.08d };
// Contains for each row the index in the discount dictionary.
        int[] ids = { 2, 0, 3, 1, 0, 2, 2, 1, 3, 1 };
// TODO: Replace with decoded discount values from `ids` and `dictionary`.

        double[] expected = new double[ids.length];

        for(int i = 0; i < expected.length; i++){
            expected[i] = dictionary[ids[i]];
        }

        return expected;
    }

    /**
     * Applies filter: (
     * discount between .05 and .07
     * and quantity < 24
     * and status = `A`
     * ) or comment LIKE `PROMO%SUMMER`
     */

    public static FilteredBatch filter(InputBatch input) {
        FilteredBatch output = new FilteredBatch();
// TODO: Replace with filter computation.

        ArrayList<Integer> keepRows = new ArrayList<>();

        for(int i = 0; i < input.numRows; i++){
            if(isLikePromoSummer(i, input.comment) || (input.discount[i] >= 0.05 && input.discount[i] <= 0.07 && input.quantity[i] < 24 && isEqualToA(i, input.status))) {
                keepRows.add(i);
            }
        }

        int newRowSize = keepRows.size();

        output.numRows = newRowSize;
        output.quantity = new int[newRowSize];
        output.price = new double[newRowSize];
        output.discount = new double[newRowSize];
        output.status.offset = new int[newRowSize];
        output.status.length = new int[newRowSize];
        output.status.buffer = new byte[newRowSize];
        output.comment.offset = new int[newRowSize];
        output.comment.length = new int[newRowSize];
        output.comment.buffer = new byte[newRowSize];

        for(int i = 0; i < newRowSize; i++){
            int validRow = keepRows.get(i);

            output.quantity[i] = input.quantity[validRow];
            output.price[i] = input.price[validRow];
            output.discount[i] = input.discount[validRow];
            output.status.offset[i] = input.status.offset[validRow];
            output.status.length[i] = input.status.length[validRow];
            output.status.buffer[i] = input.status.buffer[validRow];
            output.comment.offset[i] = input.comment.offset[validRow];
            output.comment.length[i] = input.comment.length[validRow];
            output.comment.buffer[i] = input.comment.buffer[validRow];
        }

        return output;
    }
    private static final byte byteRepA = (byte) 'A';
    public static boolean isEqualToA(int rowId, StringColumn str) {
        // TODO: Check if string is equal to "A" at given row id. Not using String is preferred.

        // if the length inside the buffer != 1, we know it's string representation cannot be "A"
        // otherwise, compare byte value to determine if it's A
        if(str.length[rowId] != 1) {
            return false;
        }
        // Note: If we assume the default charset is always UTF-8, then we can just cast to a byte...
        // otherwise we need to perform the correct conversion.
        else {
            return str.buffer[str.offset[rowId]] == byteRepA;
        }
    }

    public static boolean isLikePromoSummer(int rowId, StringColumn str) {
        //A comment is determined to be 'like' "PROMO%SUMMER" if it is atleast length 11 (PROMO = 5 & SUMMER = 6)
        //AND the first 5 bytes match the bytes of "PROMO" and the last 6 bytes match "SUMMER"
        //Since we know the byte value of each index of "PROMO" and "SUMMER", we can statically define them

        int length = str.length[rowId];

        if(length < 11) {
            return false;
        } else {
            int offset = str.offset[rowId];

            return str.buffer[offset + length - 1]  == 82 &&
                    str.buffer[offset + length - 2] == 69 &&
                    str.buffer[offset + length - 3] == 77 &&
                    str.buffer[offset + length - 4] == 77 &&
                    str.buffer[offset + length - 5] == 85 &&
                    str.buffer[offset + length - 6] == 83 &&
                    str.buffer[offset]              == 80 &&
                    str.buffer[offset + 1]          == 82 &&
                    str.buffer[offset + 2]          == 79 &&
                    str.buffer[offset + 3]          == 77 &&
                    str.buffer[offset + 4]          == 79;
        }
    }

    /**

     Sr. Software Engineer Exercise 4

     * Compute:
     * - 100 * (sum(price * discount) / sum(price)) as discount_ratio
     * - avg(price) as avg_price
     */
    public static AggregatedBatch aggregate(FilteredBatch input) {
        AggregatedBatch output = new AggregatedBatch();
        double sumPaid = 0;
        double sumPrice = 0;
        int numRows = input.numRows;

        for(int i = 0; i < numRows; i++){
            sumPaid += input.price[i] * input.discount[i];
            sumPrice += input.price[i];
        }

        output.discount_ratio = new double[]{100*(sumPaid/sumPrice)};
        output.avg_price = new double[]{sumPrice/numRows};
        return output;
    }
    private static void assertResult(double expected, double actual, String description) {
        if (Math.abs(expected - actual) > 0.000001d) {
            throw new RuntimeException("Unexpected " + description);
        }
    }
}
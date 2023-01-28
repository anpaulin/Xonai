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
//        double[] expected = new double[]{ 0.07d, 0.04d, 0.08d, 0.05d, 0.04d, 0.07d, 0.07d, 0.05d, 0.08d, 0.05d };

// I'm assuming the instructions are only asking me to fill the contents of the expected array at runtime?
        double[] expected = new double[ids.length];

        for(int i = 0; i < expected.length; i++){
            expected[i] = dictionary[ids[i]];
        }

        return expected;
    }
    private static List<Byte> convertBytesToList(byte[] bytes) {
        final List<Byte> list = new ArrayList<>();
        for (byte b : bytes) {
            list.add(b);
        }
        return list;
    }

    private static byte[] convertByteListToArray(List<Byte> bytes) {
        byte[] byteArray = new byte[bytes.size()];
        for (int index = 0; index < bytes.size(); index++) {
            byteArray[index] = bytes.get(index);
        }
        return byteArray;
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

        List<Integer> quantity = new ArrayList<>(IntStream.of(input.quantity).boxed().toList());
        List<Double> price = new ArrayList<>(DoubleStream.of(input.price).boxed().toList());
        List<Double> discount = new ArrayList<>(DoubleStream.of(input.discount).boxed().toList());
        List<Integer> offset = new ArrayList<>(IntStream.of(input.status.offset).boxed().toList());
        List<Integer> length = new ArrayList<>(IntStream.of(input.status.length).boxed().toList());
        List<Byte> statusBuffer = convertBytesToList(input.status.buffer);
        List<Integer> commentOffset = new ArrayList<>(IntStream.of(input.comment.offset).boxed().toList());
        List<Integer> commentLength = new ArrayList<>(IntStream.of(input.comment.length).boxed().toList());
        List<Byte> commentBuffer = convertBytesToList(input.status.buffer);

        int removeRows = 0;
        for(int i = 0; i < input.numRows; i++){
            boolean keepRow = isLikePromoSummer(i, input.comment) || (input.discount[i] >= 0.05 && input.discount[i] <= 0.07 && input.quantity[i] < 24 && isEqualToA(i, input.status));
            if(!keepRow) {
                removeRows++;

                price.set(i, null);
                quantity.set(i, null);
                discount.set(i, null);
                offset.set(i, null);
                length.set(i, null);
                statusBuffer.set(i, null);
                commentOffset.set(i, null);
                commentLength.set(i, null);
                commentBuffer.set(i, null);
            }
        }

        output.numRows = input.numRows - removeRows;
        output.quantity = quantity.stream().filter(Objects::nonNull).mapToInt(Integer::intValue).toArray();
        output.price = price.stream().filter(Objects::nonNull).mapToDouble(Double::doubleValue).toArray();
        output.discount = discount.stream().filter(Objects::nonNull).mapToDouble(Double::doubleValue).toArray();
        output.status.offset = offset.stream().filter(Objects::nonNull).mapToInt(Integer::intValue).toArray();
        output.status.length = length.stream().filter(Objects::nonNull).mapToInt(Integer::intValue).toArray();
        output.status.buffer = convertByteListToArray(statusBuffer.stream().filter(Objects::nonNull).toList());
        output.comment.offset = commentOffset.stream().filter(Objects::nonNull).mapToInt(Integer::intValue).toArray();
        output.comment.length = commentLength.stream().filter(Objects::nonNull).mapToInt(Integer::intValue).toArray();
        output.comment.buffer = convertByteListToArray(commentBuffer.stream().filter(Objects::nonNull).toList());

//        output.quantity = input.quantity;
//        output.price = input.price;
//        output.discount = input.discount;
//        output.status.offset = input.status.offset;
//        output.status.length = input.status.length;
//        output.status.buffer = input.status.buffer;
//        output.comment.offset = input.comment.offset;
//        output.comment.length = input.comment.length;
//        output.comment.buffer = input.comment.buffer;
        return output;
    }
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
            return str.buffer[str.offset[rowId]] == (byte) 'A';
        }
    }

    private static byte[] PROMO = "PROMO".getBytes();
    private static byte[] SUMMER = "SUMMER".getBytes();

    public static boolean isLikePromoSummer(int rowId, StringColumn str) {
// TODO: Check if comment is like "PROMO%SUMMER" at given row id. Not using regex is preferred.
        //A comment is determined to be 'like' "PROMO%SUMMER" if it is atleast length 11 (PROMO = 5 & SUMMER = 6)
        //AND the first 5 bytes match the bytes of "PROMO" and the last 6 bytes match "SUMMER"

        int length = str.length[rowId];

        if(length < 11) {
            return false;
        } else {

            for(short i = 0; i < SUMMER.length; i++){
                //check if end matches "SUMMER"
                if(str.buffer[str.offset[rowId] + length - 1 - i] != SUMMER[5 - i]){
                    return false;
                    //if it does match & we aren't out of bounds, check beginning is "PROMO"
                } else if(i < 5 && str.buffer[str.offset[rowId] + i] != PROMO[i]){
                        //check if beginning bytes match "PROMO"
                       return false;
                }
            }

            return true;
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

        for(int i = 0; i < input.numRows; i++){
            sumPaid += input.price[i] * input.discount[i];
            sumPrice += input.price[i];
        }

        output.discount_ratio = new double[]{100*(sumPaid/sumPrice)};
        output.avg_price = new double[]{sumPrice/input.numRows};
        return output;
    }
    private static void assertResult(double expected, double actual, String description) {
        if (Math.abs(expected - actual) > 0.000001d) {
            throw new RuntimeException("Unexpected " + description);
        }
    }
}
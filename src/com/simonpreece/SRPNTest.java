package com.simonpreece;

import static org.junit.Assert.*;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;


public class SRPNTest {
    protected final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
    protected final PrintStream originalOut = System.out;
    // ------- All of the above is setup, these are the actual test cases ------- //
    private final SRPN srpn = new SRPN();

    @Before
    public void setUpStreams() {
        System.setOut(new PrintStream(outContent)); // redirect System out at outContent so we can inspect it
    }

    @After
    public void restoreStreams() {
        System.setOut(originalOut);
    }

    private String getAllPrintedContent() {
        return outContent.toString();
    }

    protected List<String> getAllPrintedLinesAndRefresh() {
        List<String> printedLines =
                Arrays.stream(getAllPrintedContent().split("\n"))
                        .map(this::removeTrailingNewLine)
                        .collect(Collectors.toList());
        outContent.reset();
        return printedLines;
    }

    protected String getLatestPrintedContent() {
        List<String> lines = getAllPrintedLinesAndRefresh();
        if (!lines.isEmpty()) {
            return removeTrailingNewLine(lines.get(lines.size() - 1));
        }
        return null; // No content has been printed
    }

    protected String removeTrailingNewLine(String input) {
        if (input.endsWith("\r\n")) {
            return input.substring(0, input.length() - 2);
        }
        else if (input.endsWith("\n")) {
            return input.substring(0, input.length() - 1);
        }
        else if (input.endsWith("\r")) {
            return input.substring(0, input.length() - 1);
        }
        else {
            return input;
        }
    }

    @Test
    public void test1AdditionOverMultipleLines() {
        srpn.processCommand("10");
        srpn.processCommand("2");
        srpn.processCommand("+");
        srpn.processCommand("=");
        assertEquals("12", getLatestPrintedContent());
    }

    @Test
    public void test1SubtractionOverMultipleLines() {
        srpn.processCommand("11");
        srpn.processCommand("3");
        srpn.processCommand("-");
        srpn.processCommand("=");
        assertEquals("8", getLatestPrintedContent());
    }

    @Test
    public void test1MultiplicationOverMultipleLines() {
        srpn.processCommand("9");
        srpn.processCommand("4");
        srpn.processCommand("*");
        srpn.processCommand("=");
        assertEquals("36", getLatestPrintedContent());
    }

    @Test
    public void test1DivisionOverMultipleLines() {
        srpn.processCommand("11");
        srpn.processCommand("3");
        srpn.processCommand("/");
        srpn.processCommand("=");
        assertEquals("3", getLatestPrintedContent());
    }

    @Test
    public void test1ModulusOverMultipleLines() {
        srpn.processCommand("11");
        srpn.processCommand("3");
        srpn.processCommand("%");
        srpn.processCommand("=");
        assertEquals("2", getLatestPrintedContent());
    }

    @Test
    public void test2MultipleCommandsOverMultipleLines() {
        srpn.processCommand("3");
        srpn.processCommand("3");
        srpn.processCommand("*");
        srpn.processCommand("4");
        srpn.processCommand("4");
        srpn.processCommand("*");
        srpn.processCommand("+");
        srpn.processCommand("=");
        assertEquals("25", getLatestPrintedContent());
    }

    @Test
    public void test2DumpStackOverMultipleLines() {
        srpn.processCommand("1234");
        srpn.processCommand("2345");
        srpn.processCommand("3456");
        srpn.processCommand("d");
        String[] strArrayExpected = {"1234", "2345", "3456"};
        List<String> listExpected = Arrays.asList(strArrayExpected);
        assertEquals(listExpected, getAllPrintedLinesAndRefresh());
        srpn.processCommand("+");
        srpn.processCommand("d");
        String[] strArrayExpected2 = {"1234", "5801"};
        List<String> listExpected2 = Arrays.asList(strArrayExpected2);
        assertEquals(listExpected2, getAllPrintedLinesAndRefresh());
        srpn.processCommand("+");
        srpn.processCommand("d");
        srpn.processCommand("=");
        assertEquals("7035", getLatestPrintedContent());
    }

    @Test
    public void test3MaxSaturation() {
        srpn.processCommand("2147483647");
        srpn.processCommand("1");
        srpn.processCommand("+");
        srpn.processCommand("=");
        assertEquals("2147483647", getLatestPrintedContent());
    }

    @Test
    public void test3MinSaturation1() {
        srpn.processCommand("-2147483647");
        srpn.processCommand("1");
        srpn.processCommand("-");
        srpn.processCommand("=");
        assertEquals("-2147483648", getLatestPrintedContent());
    }

    @Test
    public void test3MinSaturation2() {
        srpn.processCommand("-2147483648");
        srpn.processCommand("1");
        srpn.processCommand("-");
        srpn.processCommand("=");
        assertEquals("-2147483648", getLatestPrintedContent());
    }

    @Test
    public void test3UnderFlow() {
        srpn.processCommand("100000");
        srpn.processCommand("0");
        srpn.processCommand("-");
        srpn.processCommand("d");
        assertEquals("100000", getLatestPrintedContent());
        srpn.processCommand("*");
        assertEquals("Stack underflow.", getLatestPrintedContent());
        srpn.processCommand("=");
        assertEquals("100000", getLatestPrintedContent());
    }

    @Test
    public void test4UnderFlow() {
        srpn.processCommand("1");
        srpn.processCommand("+");
        assertEquals("Stack underflow.", getLatestPrintedContent());
    }

    @Test
    public void test4DivideByZero() {
        srpn.processCommand("10");
        srpn.processCommand("5");
        srpn.processCommand("-5");
        srpn.processCommand("+");
        srpn.processCommand("/");
        assertEquals("Divide by 0.", getLatestPrintedContent());
    }

    @Test
    public void test4InlineStackUnderflow() {
        srpn.processCommand("11+1+1+d");
        String[] strArrayExpected = {"Stack underflow.", "13"};
        List<String> listExpected = Arrays.asList(strArrayExpected);
        assertEquals(listExpected, getAllPrintedLinesAndRefresh());
    }

    @Test
    public void test4Comments() {
        srpn.processCommand("# This is a comment #");
        assertEquals("", getLatestPrintedContent());
        srpn.processCommand("1 2 + # and so is this #");
        srpn.processCommand("d");
        assertEquals("3", getLatestPrintedContent());
    }

    @Test
    public void test4InlinePower() {
        srpn.processCommand("3 3 ^ 3 ^ 3 ^=");
        assertEquals("3", getLatestPrintedContent());
    }

    @Test
    public void test4Randoms() {
        srpn.processCommand("rrrrrrrrrrrrrrrrrrrrrrdrrrd");
        String[] strArrayExpected = {"1804289383",
                "846930886",
                "1681692777",
                "1714636915",
                "1957747793",
                "424238335",
                "719885386",
                "1649760492",
                "596516649",
                "1189641421",
                "1025202362",
                "1350490027",
                "783368690",
                "1102520059",
                "2044897763",
                "1967513926",
                "1365180540",
                "1540383426",
                "304089172",
                "1303455736",
                "35005211",
                "521595368",
                "Stack overflow.",
                "Stack overflow.",
                "1804289383",
                "846930886",
                "1681692777",
                "1714636915",
                "1957747793",
                "424238335",
                "719885386",
                "1649760492",
                "596516649",
                "1189641421",
                "1025202362",
                "1350490027",
                "783368690",
                "1102520059",
                "2044897763",
                "1967513926",
                "1365180540",
                "1540383426",
                "304089172",
                "1303455736",
                "35005211",
                "521595368",
                "1804289383"};
        List<String> listExpected = Arrays.asList(strArrayExpected);
        assertEquals(listExpected, getAllPrintedLinesAndRefresh());
    }

    @Test
    public void extraTestCommentWithNoSpaceAtStart() {
        srpn.processCommand("#Comment #+12");
        String[] strArrayExpected = {
                "Unrecognised operator or operand \"#\".",
                "Unrecognised operator or operand \"C\".",
                "Unrecognised operator or operand \"o\".",
                "Unrecognised operator or operand \"m\".",
                "Unrecognised operator or operand \"m\".",
                "Unrecognised operator or operand \"e\".",
                "Unrecognised operator or operand \"n\".",
                "Unrecognised operator or operand \"t\".",
                "Unrecognised operator or operand \"#\".",
                "Stack underflow."};
        List<String> listExpected = Arrays.asList(strArrayExpected);
        assertEquals(listExpected, getAllPrintedLinesAndRefresh());
    }

    @Test
    public void extraTestCommentWithNoSpaceAtEnd() {
        srpn.processCommand("# Comment# +12=");
        assertEquals("", getLatestPrintedContent());
    }

    @Test
    public void extraTestCommentSpansLines() {
        srpn.processCommand("# this is a comment");
        srpn.processCommand("so is this # +12");
        assertEquals("Stack underflow.", getLatestPrintedContent());
    }

    @Test
    public void extraTestPlusWithSpaceAfter() {
        srpn.processCommand("10 10 + 5 + 5");
        srpn.processCommand("d");
        String[] strArrayExpected = {
                "25",
                "5"};
        List<String> listExpected = Arrays.asList(strArrayExpected);
        assertEquals(listExpected, getAllPrintedLinesAndRefresh());
    }

    @Test
    public void extraTestPlusWithNoSpaceAfter() {
        srpn.processCommand("10 10 +5 +5");
        srpn.processCommand("d");
        String[] strArrayExpected = {
                "10",
                "20"};
        List<String> listExpected = Arrays.asList(strArrayExpected);
        assertEquals(listExpected, getAllPrintedLinesAndRefresh());
    }

    @Test
    public void extraTestCombinedRPNandPNwithBODMAS() {
        srpn.processCommand("1 2 3 +5*3");
        srpn.processCommand("d");
        String[] strArrayExpected = {"1", "2", "18"};
        List<String> listExpected = Arrays.asList(strArrayExpected);
        assertEquals(listExpected, getAllPrintedLinesAndRefresh());
    }

    @Test
    public void extraTestCombinedRPNandPNwithBODMAS2() {
        srpn.processCommand("1 2 3+ 5*3");
        srpn.processCommand("d");
        String[] strArrayExpected = {"1", "5", "15"};
        List<String> listExpected = Arrays.asList(strArrayExpected);
        assertEquals(listExpected, getAllPrintedLinesAndRefresh());
    }

    @Test
    public void extraTestInlineStackAndOperatorOverload() {
        srpn.processCommand("1");
        srpn.processCommand("2");
        srpn.processCommand("3");
        srpn.processCommand("4");
        srpn.processCommand("5");
        srpn.processCommand("6");
        srpn.processCommand("7");
        srpn.processCommand("8");
        srpn.processCommand("9");
        srpn.processCommand("0");
        srpn.processCommand("1");
        srpn.processCommand("2");
        srpn.processCommand("3");
        srpn.processCommand("4");
        srpn.processCommand("5");
        srpn.processCommand("6");
        srpn.processCommand("7");
        srpn.processCommand("8");
        srpn.processCommand("9");
        srpn.processCommand("0");
        srpn.processCommand("1+2+3+4+5+6");
        String[] strArrayExpected = {"Stack overflow.", "Stack overflow.", "Stack overflow."};
        List<String> listExpected = Arrays.asList(strArrayExpected);
        assertEquals(listExpected, getAllPrintedLinesAndRefresh());
        srpn.processCommand("d");
        String[] strArrayExpected2 = {"1", "2", "3", "4", "5", "6", "7", "8", "9", "0", "1", "2", "3", "4", "5", "6", "7", "23"};
        List<String> listExpected2 = Arrays.asList(strArrayExpected2);
        assertEquals(listExpected2, getAllPrintedLinesAndRefresh());
    }

    @Test
    public void extraTestInlineStackDoesntOverflowIfNumbersInOneStatement() {
        srpn.processCommand("1");
        srpn.processCommand("2");
        srpn.processCommand("3");
        srpn.processCommand("4");
        srpn.processCommand("5");
        srpn.processCommand("6");
        srpn.processCommand("7");
        srpn.processCommand("8");
        srpn.processCommand("9");
        srpn.processCommand("0");
        srpn.processCommand("1");
        srpn.processCommand("2");
        srpn.processCommand("3");
        srpn.processCommand("4");
        srpn.processCommand("5");
        srpn.processCommand("6");
        srpn.processCommand("7");
        srpn.processCommand("8");
        srpn.processCommand("9");
        srpn.processCommand("0");
        srpn.processCommand("1+2+3+ 5");
        srpn.processCommand("d");
        String[] strArrayExpected2 = {"1", "2", "3", "4", "5", "6", "7", "8", "9", "0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "6", "5"};
        List<String> listExpected2 = Arrays.asList(strArrayExpected2);
        assertEquals(listExpected2, getAllPrintedLinesAndRefresh());
    }

    @Test
    public void extraTestInlineStackDoesntOverflowAndBodmas() {
        srpn.processCommand("1");
        srpn.processCommand("2");
        srpn.processCommand("3");
        srpn.processCommand("4");
        srpn.processCommand("5");
        srpn.processCommand("6");
        srpn.processCommand("7");
        srpn.processCommand("8");
        srpn.processCommand("9");
        srpn.processCommand("0");
        srpn.processCommand("1");
        srpn.processCommand("2");
        srpn.processCommand("3");
        srpn.processCommand("4");
        srpn.processCommand("5");
        srpn.processCommand("6");
        srpn.processCommand("7");
        srpn.processCommand("8");
        srpn.processCommand("9");
        srpn.processCommand("0");
        srpn.processCommand("1-2*3 5+ 6+");
        srpn.processCommand("d");
        String[] strArrayExpected2 = {"1", "2", "3", "4", "5", "6", "7", "8", "9", "0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "0", "6"};
        List<String> listExpected2 = Arrays.asList(strArrayExpected2);
        assertEquals(listExpected2, getAllPrintedLinesAndRefresh());
    }

    @Test
    public void extraTestInlineStackDoesntOverflowAndBodmasRPN() {
        srpn.processCommand("1");
        srpn.processCommand("2");
        srpn.processCommand("3");
        srpn.processCommand("4");
        srpn.processCommand("5");
        srpn.processCommand("6");
        srpn.processCommand("7");
        srpn.processCommand("8");
        srpn.processCommand("9");
        srpn.processCommand("0");
        srpn.processCommand("1");
        srpn.processCommand("2");
        srpn.processCommand("3");
        srpn.processCommand("4");
        srpn.processCommand("5");
        srpn.processCommand("6");
        srpn.processCommand("7");
        srpn.processCommand("8");
        srpn.processCommand("9");
        srpn.processCommand("0");
        srpn.processCommand("1-2*3+ 5+ 6+");
        srpn.processCommand("d");
        String[] strArrayExpected2 = {"1", "2", "3", "4", "5", "6", "7", "8", "9", "0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "6"};
        List<String> listExpected2 = Arrays.asList(strArrayExpected2);
        assertEquals(listExpected2, getAllPrintedLinesAndRefresh());
    }

    @Test
    public void extraTestDetermineInlineStackSize() {
        srpn.processCommand("1");
        srpn.processCommand("2");
        srpn.processCommand("3");
        srpn.processCommand("4");
        srpn.processCommand("5");
        srpn.processCommand("6");
        srpn.processCommand("7");
        srpn.processCommand("8");
        srpn.processCommand("9");
        srpn.processCommand("0");
        srpn.processCommand("1");
        srpn.processCommand("2");
        srpn.processCommand("3");
        srpn.processCommand("4");
        srpn.processCommand("5");
        srpn.processCommand("6");
        srpn.processCommand("7");
        srpn.processCommand("8");
        srpn.processCommand("9");
        srpn.processCommand("0");
        srpn.processCommand("1");
        srpn.processCommand("2");
        srpn.processCommand("3");
        srpn.processCommand("---------------------- 3+3+3");
        srpn.processCommand("d");
        String[] strArrayExpected = {"12", "9"};
        List<String> listExpected = Arrays.asList(strArrayExpected);
        assertEquals(listExpected, getAllPrintedLinesAndRefresh());
    }

    @Test
    public void extraTestDetermineInlineStackSize2() {
        srpn.processCommand("1");
        srpn.processCommand("2");
        srpn.processCommand("3");
        srpn.processCommand("4");
        srpn.processCommand("5");
        srpn.processCommand("6");
        srpn.processCommand("7");
        srpn.processCommand("8");
        srpn.processCommand("9");
        srpn.processCommand("0");
        srpn.processCommand("1");
        srpn.processCommand("2");
        srpn.processCommand("3");
        srpn.processCommand("4");
        srpn.processCommand("5");
        srpn.processCommand("6");
        srpn.processCommand("7");
        srpn.processCommand("8");
        srpn.processCommand("9");
        srpn.processCommand("0");
        srpn.processCommand("1");
        srpn.processCommand("2");
        srpn.processCommand("3");
        srpn.processCommand("---------------------- 1+2+3+4+5+6+7+8+9+0+1+2+3+4+5+6+7+8+9+0 1+2+3+4+5+6+7+8+9+0+1+2+3+4+5+6+7+8");
        srpn.processCommand("d");
        String[] strArrayExpected = {"12", "90", "81"};
        List<String> listExpected = Arrays.asList(strArrayExpected);
        assertEquals(listExpected, getAllPrintedLinesAndRefresh());
    }

    @Test
    public void extraTestMultipleCommandBlocks() {
        srpn.processCommand("10 10 15+ 2*3 10 2*3 10+");
        srpn.processCommand("d");
        String[] strArrayExpected = {"10", "25", "6", "10", "16"};
        List<String> listExpected = Arrays.asList(strArrayExpected);
        assertEquals(listExpected, getAllPrintedLinesAndRefresh());
    }

    @Test
    public void extraTestRepeatMultipleCommandBlocks() {
        srpn.processCommand("+10 1020");
        assertEquals("Stack underflow.", getLatestPrintedContent());
        srpn.processCommand("d");
        String[] strArrayExpected = {"10", "1020"};
        List<String> listExpected = Arrays.asList(strArrayExpected);
        assertEquals(listExpected, getAllPrintedLinesAndRefresh());
        srpn.processCommand("+10 1020");
        srpn.processCommand("d");
        String[] strArrayExpected2 = {"10", "1030", "1020"};
        List<String> listExpected2 = Arrays.asList(strArrayExpected2);
        assertEquals(listExpected2, getAllPrintedLinesAndRefresh());
    }

    @Test
    public void extraTestEqualsMessesUpCalculation() {
        srpn.processCommand("10+2d5*3");
        assertEquals("12", getLatestPrintedContent());
        srpn.processCommand("10+2=5*3");
        assertEquals("2", getLatestPrintedContent());
        srpn.processCommand("d");
        String[] strArrayExpected = {"12", "15", "10", "17"};
        List<String> listExpected = Arrays.asList(strArrayExpected);
        assertEquals(listExpected, getAllPrintedLinesAndRefresh());
    }
    @Test
    public void extraTestEqualsMessesUpCalculation2() {
        srpn.processCommand("10*2=5+3");
        assertEquals("2", getLatestPrintedContent());
        srpn.processCommand("d");
        String[] strArrayExpected = {"10", "13"};
        List<String> listExpected = Arrays.asList(strArrayExpected);
        assertEquals(listExpected, getAllPrintedLinesAndRefresh());
    }


    @Test
    public void extraTestPowExecutesBeforeModulus() {
        srpn.processCommand("4%1^2d");
        assertEquals("0", getLatestPrintedContent());
    }

    @Test
    public void extraTestModulusExecutesBeforeDivision() {
        srpn.processCommand("3%4/2d");
        assertEquals("1", getLatestPrintedContent());
        srpn.processCommand("4/4%2");
        assertEquals("Divide by 0.", getLatestPrintedContent());
    }

    @Test
    public void extraTestMinusNegative20GivesWrongOutput() {
        srpn.processCommand("10");
        srpn.processCommand("5");
        srpn.processCommand("--20");
        srpn.processCommand("d");
        String[] strArrayExpected = {"25"};
        List<String> listExpected = Arrays.asList(strArrayExpected);
        assertEquals(listExpected, getAllPrintedLinesAndRefresh());
    }
    @Test
    public void extraTestMinusInMultipleOperationsDOESWork() {
        srpn.processCommand("5--2");
        srpn.processCommand("d");
        String[] strArrayExpected = {"7"};
        List<String> listExpected = Arrays.asList(strArrayExpected);
        assertEquals(listExpected, getAllPrintedLinesAndRefresh());
    }
    @Test
    public void extraTestPlusDoubleMinusAtEndOfCalc() {
        srpn.processCommand("33");
        srpn.processCommand("20");
        srpn.processCommand("10");
        srpn.processCommand("+--4");
        srpn.processCommand("d");
        String[] strArrayExpected = {"7"};
        List<String> listExpected = Arrays.asList(strArrayExpected);
        assertEquals(listExpected, getAllPrintedLinesAndRefresh());
    }
    @Test
    public void extraTestMultiplyDoubleMinusAtEndOfCalc() {
        srpn.processCommand("2");
        srpn.processCommand("8");
        srpn.processCommand("16");
        srpn.processCommand("*--2");
        srpn.processCommand("d");
        String[] strArrayExpected = {"-124"};
        List<String> listExpected = Arrays.asList(strArrayExpected);
        assertEquals(listExpected, getAllPrintedLinesAndRefresh());
    }
    @Test
    public void extraTestDivideDoubleMinusAtEndOfCalc() {
        srpn.processCommand("10");
        srpn.processCommand("2");
        srpn.processCommand("6");
        srpn.processCommand("/--6");
        srpn.processCommand("d");
        String[] strArrayExpected = {"12"};
        List<String> listExpected = Arrays.asList(strArrayExpected);
        assertEquals(listExpected, getAllPrintedLinesAndRefresh());
    }
    @Test
    public void extraTestCaratDoubleMinusAtEndOfCalc() {
        srpn.processCommand("22");
        srpn.processCommand("6");
        srpn.processCommand("8");
        srpn.processCommand("^--5");
        srpn.processCommand("d");
        String[] strArrayExpected = {"-1679589"};
        List<String> listExpected = Arrays.asList(strArrayExpected);
        assertEquals(listExpected, getAllPrintedLinesAndRefresh());
    }
    @Test
    public void extraTestModulusDoubleMinusAtEndOfCalc() {
        srpn.processCommand("56");
        srpn.processCommand("3");
        srpn.processCommand("67");
        srpn.processCommand("%--5");
        srpn.processCommand("d");
        String[] strArrayExpected = {"70"};
        List<String> listExpected = Arrays.asList(strArrayExpected);
        assertEquals(listExpected, getAllPrintedLinesAndRefresh());
    }
    @Test
    public void extraTestDoubleMinusOnSeparateLineWithSingleNumInStackUnderflows() {
        srpn.processCommand("5");
        srpn.processCommand("--2");
        assertEquals("Stack underflow.", getLatestPrintedContent());
        srpn.processCommand("d");
        String[] strArrayExpected = {"3"};
        List<String> listExpected = Arrays.asList(strArrayExpected);
        assertEquals(listExpected, getAllPrintedLinesAndRefresh());
    }
    @Test
    public void extraTestDoubleMinusOnSingleLine() {
        srpn.processCommand("5--2");
        srpn.processCommand("d");
        String[] strArrayExpected = {"7"};
        List<String> listExpected = Arrays.asList(strArrayExpected);
        assertEquals(listExpected, getAllPrintedLinesAndRefresh());
    }
    @Test
    public void extraOperatorsReceivedConsecutivelyMultipleDivide() {
        srpn.processCommand("20");
        srpn.processCommand("5--8*/25");
        srpn.processCommand("d");
        String[] strArrayExpected = {"21"};
        List<String> listExpected = Arrays.asList(strArrayExpected);
        assertEquals(listExpected, getAllPrintedLinesAndRefresh());
    }
    @Test
    public void extraOperatorsReceivedConsecutivelyDivideMultiple() {
        srpn.processCommand("20");
        srpn.processCommand("5--8/*25");
        srpn.processCommand("d");
        String[] strArrayExpected = {"21", "129"};
        List<String> listExpected = Arrays.asList(strArrayExpected);
        assertEquals(listExpected, getAllPrintedLinesAndRefresh());
    }

    @Test
    public void extraTestOddResultProvingDoublesUsed() {
        srpn.processCommand("1");
        srpn.processCommand("2");
        srpn.processCommand("3");
        srpn.processCommand("/");
        srpn.processCommand("-");
        srpn.processCommand("=");
        assertEquals("0", getLatestPrintedContent());
    }

    @Test
    public void extraTestCommandBlockswithMultipleSequentialOperators() {
        srpn.processCommand("457 159 54 448 -/*");
        srpn.processCommand("d");
        assertEquals("72607", getLatestPrintedContent());
    }

    @Test
    public void extraTestCommandBlockswithMultipleSequentialOperators2() {
        srpn.processCommand("457 159 54 448 /-*");
        srpn.processCommand("d");
        assertEquals("437", getLatestPrintedContent());
    }

    @Test
    public void extraTestUnrecognisedOperatorTriggersImmediatelyButDoesNotExecuteStack() {
        srpn.processCommand("5+5g*10d");
        String[] strArrayExpected = {"Unrecognised operator or operand \"g\".", "55"};
        List<String> listExpected = Arrays.asList(strArrayExpected);
        assertEquals(listExpected, getAllPrintedLinesAndRefresh());
    }

    /*

        assertEquals("", getLatestPrintedContent());

     */


}

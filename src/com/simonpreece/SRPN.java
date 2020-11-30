package com.simonpreece;

import java.util.Scanner;
import java.util.Stack;

/**
 * Program class for an SRPN calculator.
 * Requirements - (from the Coursework description) "SRPN is a reverse Polish notation calculator with the extra feature
 * that all arithmetic is saturated, i.e. when it reaches the maximum value that can be stored in a variable, it stays
 * at the maximum rather than wrapping around...Your task is to write a program which matches the functionality of SRPN
 * as closely as possible. Note that this includes not adding or enhancing existing features."
 * <p>
 * <p>
 * Standard RPN behaviour - to be matched
 * <p>
 * Standard Reverse Polish Notation description: https://en.wikipedia.org/wiki/Reverse_Polish_notation
 * Reverse Polish Notation takes the values first and then the operator and prior to executing; any sums are added to a stack which is executed and condensed back to a zero stack according to subsequent s IMPROVE DESCRIPTION
 * =============================================================================
 * Program specific behaviour by original SRPN calculator - to be replicated
 * =============================================================================
 * # When first opened the prompt displayed is "You can now start interacting with the SRPN calculator".
 * # The tool mimics using Binary signed 2's complement integer to store values (Min = -2147483648, Max =  2147483647)
 * HOWEVER although results are displayed as integers and integer division is mimicked (i.e. 5 2 / displays 2),
 * if the user multiplies the result by 2 again they get back to 5,
 * meaning that the value stored on the stack is a real number.
 * # The tool copes with saturation by making any number entered or calculation arrived at that goes beyond the above
 * min/max equal to the min/max
 * i.e. -2147483648 - 1 = -2147483648 and
 * 2147483647 + 1 = 2147483647
 * # Warning displayed if receives an unknown operator (e.g. 'g') is 'Unrecognised operator or operand "g".'
 * The action is discarded, but this does not reset the stack or prevent further actions when they are being
 * entered one at a time.
 * # Stack in the original is 23 elements deep.
 * # Displays warning "Stack overflow." when trying to put an entry on the stack beyond the maximum stack size; this
 * discards any items added but does not reset the stack or prevent further actions which would not increase stack size.
 * # Displays warning "Stack underflow." when trying to perform operations without enough entries (2) on the stack;
 * this discards any actions added but does not reset the stack or prevent further actions.
 * # Entering equals '=' outputs the value held at the top of the stack at that point in the calculation.
 * # If '=' is entered with nothing in the stack "Stack empty." is displayed.
 * # Dividing by zero gives a handled error "Divide by 0."
 * # Using Modulus by zero throw an UN-handled error and EXITS the program
 * # 'd' displays each item in the stack, from first position to last on a new line per item with no additional formatting.
 * # If "d" is entered with an empty stack '-2147483648' is displayed. REPLICATE THIS
 * # Entering '#' seems to turn on and off commenting - anything entered between 2 x # symbols is ignored; this occurs whether the entries are on single or multiple lines.
 * # "^" is 'to the power of'
 * # If entering a string that includes an unrecognised character (i.e. "2*2l4") the "l" in this case seems to be substituted by the previous operator "*" and the output to the stack is 2 then 8 INVESTIGATE MORE
 * # a newline on its own is ignored and does not trigger an error or warning
 * # leading and trailing whitespace is ignored
 * # 'r' generates a 'pseudo-random' number which is actually a number from the following list of 32 integers which cycles through in order:
 * 1804289383
 * 846930886
 * 1681692777
 * 1714636915
 * 1957747793
 * 424238335
 * 719885386
 * 1649760492
 * 596516649
 * 1189641421
 * 1025202362
 * 1350490027
 * 783368690
 * 1102520059
 * 2044897763
 * 1967513926
 * 1365180540
 * 1540383426
 * 304089172
 * 1303455736
 * 35005211
 * 521595368
 * <p>
 * Data validity checks are performed in the following order:
 * 1. check if the operator is recognised and error if not
 * 2. if the command is "d" or "=" output results specified and stop
 * 3a. if the operator is valid and there is more than one item in the stack
 * i) perform calculation
 * ii) place the result in position-1
 * iii) move the position pointer back one place
 * =============================================================================
 */
@SuppressWarnings({"Convert2streamapi", "ForLoopReplaceableByForEach"})
public class SRPN {

    @SuppressWarnings("FieldCanBeLocal")
    private final int calcStack_MAXSIZE = 23;
    private final Stack<Double> rp_NumberStack = new Stack<>();
    private final Stack<Character> inlineExecutionStack = new Stack<>();
    // rArray holds the values for the pseudo-random function 'r'. (stored in main class as liable to change?)
    private final double[] rArray = new double[]{1804289383, 846930886, 1681692777, 1714636915, 1957747793, 424238335,
            719885386, 1649760492, 596516649, 1189641421, 1025202362, 1350490027, 783368690, 1102520059, 2044897763,
            1967513926, 1365180540, 1540383426, 304089172, 1303455736, 35005211, 521595368};
    boolean inCommentMode = false;
    private int rArray_position = 0;
    private double firstNum, secondNum;
    // private boolean debugMode = false; // USED FOR DEBUGGING ONLY

    public SRPN() {
        // Nothing special in the constructor.
    }

    /**
     * Function that receives the initial command and decodes it, dictating the order of execution.
     *
     * @param s - string to be evaluated - should comprise of long integers and recognised operators
     *          ( ^ % / * + - r d = )
     */
    public void processCommand(String s) {
        Scanner CommandScanner = new Scanner(s);
        String commandBlock;
        char currentChar;
        double currentNum, currentNumInChar, currentNumToStore;
        int currentTopOfStack;
        boolean isNumToStore, isFirstChar, lastCharWasOperator,
                minusReceivedAfterOtherOperator, lastCharWasMinus, twoMinusInARow;

        if (s.length() > 0) { // Ignore empty lines as nothing to do.
            do {
                currentTopOfStack = rp_NumberStack.size() - 1;
                commandBlock = CommandScanner.next(); // Execute each set of instructions between whitespace separately.
                // Reset following for each CommandBlock, as they process separately.
                currentNumToStore = 0;
                isNumToStore = false;
                isFirstChar = true;
                lastCharWasOperator = false;
                minusReceivedAfterOtherOperator = false;
                lastCharWasMinus = false;
                twoMinusInARow = false;
                try {
                    // Check if commandBlock is a valid long and throw an Exception if not.
                    currentNum = Long.parseLong(commandBlock);
                    //noinspection StatementWithEmptyBody
                    if (!inCommentMode) {
                        /* Add to stack if there is space and the number does NOT start with a '+' (for parseLong this
                            means a number is positive, but not in the original SRPN, where it means 'add results to
                            current top of stack'. */
                        if (commandBlock.charAt(0) == '+') {
                            rp_NumberStack.set(currentTopOfStack,
                                    saturateNumber(rp_NumberStack.get(currentTopOfStack) + currentNum));
                        }
                        else if (isWithinStackRange(true, false)) {
                            rp_NumberStack.push(saturateNumber(currentNum));
                        }
                        lastCharWasOperator = false;
                    }
                    else {
                        // Do nothing as currently in comment mode.
                    }
                } catch (Exception e) {
                    // Command block is a non-integer string or a mixture of text and numbers.
                    currentChar = commandBlock.charAt(0);
                    if (commandBlock.length() == 1 && currentChar == '#') { /* CommentMode on/off is ONLY triggered if
                                                                                  the '#' is on its own in a block. */
                        inCommentMode = !inCommentMode;
                    }
                    else {
                        // Loop through characters in commandBlock.
                        for (int i = 0; i < commandBlock.length(); i++) {
                            currentChar = commandBlock.charAt(i);
                            if (!inCommentMode) {
                                try {
                                    currentNumInChar = Double.parseDouble(String.valueOf(currentChar));
                                    isNumToStore = true;
                                    // Add the current char number to the running total to add to stack later.
                                    currentNumToStore = currentNumToStore * 10 + currentNumInChar;
                                    lastCharWasOperator = false;
                                } catch (Exception ee) { // The character received is not a number.
                                    /* Check if currentChar has a BODMAS priority and is therefore a valid operator
                                                                                         - display warning if not. */
                                    if (bodmasPriority(currentChar) == 0) {
                                        System.out.println("Unrecognised operator or operand \"" + currentChar + "\".");
                                    }
                                    else {
                                        if (isNumToStore) { /* Check if have already previously been passed a number, if so
                                                                add it to the stack and reset NumToStore variables. */
                                            if (isWithinStackRange(true, false)) {
                                                writeNumberToStack(currentNumToStore, twoMinusInARow, minusReceivedAfterOtherOperator);
                                            }
                                            minusReceivedAfterOtherOperator = false;
                                            lastCharWasMinus = false;
                                            /* The original 'loses' the number at this point if it can't save it, so leave
                                                                                    following outside the check. */
                                            currentNumToStore = 0;
                                            isNumToStore = false;
                                        }// - End of NumStore

                                        twoMinusInARow = (lastCharWasMinus && currentChar == '-');

                                        if (minusReceivedAfterOtherOperator) {
                                            if (currentChar == '-') {/* two minuses in a row after another operator - definitely
                                             execute inlineExecutionStack */
                                                executeInlineExecutionStack(true);
                                            }
                                            else { /* the previous minus did not turn out to be a number sign - check if
                                             the stack should have been executed (compare BODMAS of previous two chars). */
                                                if (inlineExecutionStack.size() > 1 &&
                                                        bodmasPriority(inlineExecutionStack.get(inlineExecutionStack.size() - 2)) >
                                                                bodmasPriority(inlineExecutionStack.peek())) {
                                                    executeInlineExecutionStack(true);
                                                }
                                                minusReceivedAfterOtherOperator = false;
                                            }
                                        }

                                        if (lastCharWasOperator && !lastCharWasMinus && currentChar == '-') { /* If a Minus is received after
                                            another operator it could be a negative number sign, do nothing for now
                                            but note that the condition has happened. */
                                            minusReceivedAfterOtherOperator = true;
                                        }
                                        if (isFirstChar && currentChar == '-') {
                                            minusReceivedAfterOtherOperator = true; /* Original treats minuses at the start of a commandBlock as if they were following another operator */
                                        }

                                        if (currentChar == 'd') {
                                            executeInlineExecutionStack(false); /* In original 'd' triggers an
                                                execution of the stack so far and displays the stack. */
                                            displayStack();
                                        }
                                        if (currentChar == '=') {
                                        /* command has received an equals - in the original this runs immediately,
                                         displaying the number at the top of the stack prior to any further calculations
                                         ; hence checking for it here. */
                                            if (rp_NumberStack.size() > 0) {
                                                System.out.println(rp_NumberStack.peek().intValue());
                                            }
                                            else {
                                                System.out.println("Stack empty.");
                                            }
                                        }

                                    /* If have NOT received a minus after an operator (if so do nothing for now), and
                                        the current character is a valid operator other than 'equals'; check if the stack
                                        should be executed. Stack should be executed if the previous operators 'BODMAS'
                                        order of execution was greater than the current operator (i.e. * > +). */
                                        if (!minusReceivedAfterOtherOperator &&
                                                !inlineExecutionStack.isEmpty() &&
                                                currentChar != '=' &&
                                                bodmasPriority(inlineExecutionStack.peek()) > bodmasPriority(currentChar)) {
                                            executeInlineExecutionStack(false);
                                        }

                                        lastCharWasOperator = true;
                                        lastCharWasMinus = (currentChar == '-');
                                        inlineExecutionStack.push(currentChar);
                                    }//--end of Bodmas Operator Validity Check
                                }//--end of for loop parseDouble exception
                            }//--end of Comment mode check
                            isFirstChar = false;
                        }//--end of single character loop.
                    }//--end of check for comment mode.

                    /* Execute any valid command at the end of a block. */
                    if (bodmasPriority(currentChar) > 0 && bodmasPriority(currentChar) <= 6 && !inCommentMode) {
                        performRP_Calculation(inlineExecutionStack.pop());
                    }
                }//-- End of commandBlock number check exception.

                /* If the commandBlock contained more than one character process the instructions- execution is skipped
                    if first commandBlock is just an operator.*/
                if (!isFirstChar) {
                    if (isNumToStore) {
                        if (isWithinStackRange(true, false)) {
                            writeNumberToStack(currentNumToStore, twoMinusInARow, minusReceivedAfterOtherOperator);
                        }
                    }
                    /* Execute the entire command block as have reached its end. */
                    executeInlineExecutionStack(false);
                }
            } while (CommandScanner.hasNext()); /* If string has another commandBlock, loop back and do again. */
        }
    }

    /**
     * Write number to stack
     *
     * @param currentNumToStore               Number to add
     * @param twoMinusInARow                  Directly preceding the number, were there two minuses in a row?
     * @param minusReceivedAfterOtherOperator Directly preceding the two minuses, was there another operator
     *                                        or was it the start of CommandBlock?
     */
    private void writeNumberToStack(double currentNumToStore, boolean twoMinusInARow, boolean minusReceivedAfterOtherOperator) {
        if (twoMinusInARow) {
            if (minusReceivedAfterOtherOperator) {
                rp_NumberStack.push(currentNumToStore); /* Original SRPN ignores possible negative number polarity if
                   two minuses follow each other directly after another operator or are at the start of a commandBlock */
            }
            else {
                rp_NumberStack.push(currentNumToStore * -1);
                inlineExecutionStack.pop();  /* Get rid of the minus from the top of the stack as it was a number
                                                                                            sign not an operator. */
            }
        }
        else {
            if (minusReceivedAfterOtherOperator) {
                rp_NumberStack.push(currentNumToStore * -1);
                inlineExecutionStack.pop();
            }
            else {
                rp_NumberStack.push(currentNumToStore);
            }
        }
    }


    /**
     * Execute all instructions in the Execution Stack.
     *
     * @param skipTopInstruction - If 'true' execution starts one down from the top (used for retrospectively executing
     *                           the stack if a minus sign turned out to be an operator, and not a negative number sign).
     */
    private void executeInlineExecutionStack(boolean skipTopInstruction) {
        char TopInstruction = 0;

        if (skipTopInstruction) {
            TopInstruction = inlineExecutionStack.pop();
        }
        while (inlineExecutionStack.size() > 0) {
            performRP_Calculation(inlineExecutionStack.pop());
        }
        if (skipTopInstruction) {
            inlineExecutionStack.push(TopInstruction); //put the instruction skipped back on the top of the stack.
        }
    }


    /**
     * Function to give a numerical priority for a given operator to calculate BODMAS order of execution, also used to
     * determine if a character is a valid operator (if bodmasPriority > 0 then operator is valid).
     *
     * @param operator char containing the operator to be assessed.
     * @return integer with the BODMAS priority - the higher the value the higher the priority.
     */
    private int bodmasPriority(char operator) {
        int priority = 0;
        switch (operator) {
            case 'd':
                priority++;
/* DEBUG FUNCTIONALITY
            case 'p':  //!!!!REMOVE BEFORE GOING TO PRODUCTION!!!!
                priority++;
            case 'b':  //!!!!REMOVE BEFORE GOING TO PRODUCTION!!!!
                priority++;
            case 'c':  //!!!!REMOVE BEFORE GOING TO PRODUCTION!!!!
                priority++;
*/
            case 'r':
                priority++;
            case '^':
                priority++;
            case '%':
                priority++;
            case '/':
                priority++;
            case '*':
                priority++;
            case '=':
                priority++;
            case '+':
                priority++;
            case '-':
                priority++;
        }
        return priority;
    }


    /**
     * Function receives a single character operator and executes against numbers on the stack, using SRPN logic
     * as detailed in the main API comments
     *
     * @param operator - character to be analysed and checked for possible operations
     */
    private void performRP_Calculation(char operator) {
        double currentNum = 0;
        boolean isCalcPerformed = false;
        int topNumStackIndex = rp_NumberStack.size() - 1;
        switch (operator) {
            case 'd':// do nothing; these are valid operators but their execution is governed in processCommand.
            case '=':
                break;
/*      This would be a better place to check for comment mode, but the original only applies it if the '#' character is
            on its own and not inside a commandBlock.
            case '#':
                inCommentMode = true; //turn on comment mode
                break;
*/
            case 'r': //add pseudo-random number to stack.
                if (isWithinStackRange(true, false)) {
                    rp_NumberStack.push(rArray[rArray_position]);
                    //increment rArray position by 1 and wrap back to zero when end of array reached.
                    rArray_position = ++rArray_position % rArray.length;
                }
                break;
/* Following are used for debugging only
            case 'c': //!!!!REMOVE BEFORE GOING TO PRODUCTION!!!!
                DEBUG_Reset_ALL();
                break;
            case 'p':  //!!!!REMOVE BEFORE GOING TO PRODUCTION!!!!
                DEBUG_printCalcStack();
                break;
            case 'b':  //!!!!REMOVE BEFORE GOING TO PRODUCTION!!!!
                debugMode = !debugMode;
                break;
*/
            case '+':
                if (isWithinStackRange(false, true)) { /*only perform operation if there is more
                                    than one integer in the stack - check performed here to be inline with original.*/
                    assignNums(); /*done at this stage rather than once earlier to avoid under-runs for non-'normal'
                                    operators and to stay true to the order of checks in SRPN original.*/
                    currentNum = firstNum + secondNum;
                    isCalcPerformed = true;
                }
                break;
            case '-':
                if (isWithinStackRange(false, true)) {
                    assignNums();
                    currentNum = firstNum - secondNum;
                    isCalcPerformed = true;
                }
                break;
            case '*':
                if (isWithinStackRange(false, true)) {
                    assignNums();
                    currentNum = firstNum * secondNum;
                    isCalcPerformed = true;
                }
                break;
            case '/':
                if (isWithinStackRange(false, true)) {
                    assignNums();
                    if (secondNum == 0) {
                        System.out.println("Divide by 0.");
                        break;
                    }
                    currentNum = firstNum / secondNum;
                    isCalcPerformed = true;
                }
                break;
            case '%':
                if (isWithinStackRange(false, true)) {
                    assignNums();
                    // Original throws the error below if it encounters zero as the second number in a modulus operation.
                    if (secondNum == 0) {
                        throw new java.lang.ArithmeticException(
                                "main.sh: line 5:    53 Floating point exception(core dumped) ./srpn/srpn");
                    }
                    currentNum = firstNum % secondNum;
                    isCalcPerformed = true;
                }
                break;
            case '^':
                if (isWithinStackRange(false, true)) {
                    assignNums();
                    currentNum = Math.pow(firstNum, secondNum);
                    isCalcPerformed = true;
                }
                break;
            default:  /* not technically required as the processCommand function should guard against invalid operators,
                            but left in for defensive programming. */
                System.out.println("Unrecognised operator or operand \"" + operator + "\".");
        }
        // only perform following if executed a calculation ('=', 'd', 'r' etc do not count).
        if (isCalcPerformed) {
            // remove the top two items from the stack and replace with the calculation performed.
            rp_NumberStack.set(topNumStackIndex - 1, currentNum);
            rp_NumberStack.removeElementAt(topNumStackIndex);
        }
    }


    /**
     * Function to limit output of a number to within the range set in the class variables output_MIN and output_MAX
     *
     * @param input - number to be checked
     * @return - number limited within range
     */
    private double saturateNumber(Double input) {
        if (input <= Integer.MIN_VALUE) {
            //noinspection UnnecessaryBoxing
            input = Double.valueOf(Integer.MIN_VALUE);
        }
        else if (input >= Integer.MAX_VALUE) {
            //noinspection UnnecessaryBoxing
            input = Double.valueOf(Integer.MAX_VALUE);
        }
        return input;
    }


    /**
     * Display the current stack on a new line per entry with no other formatting.
     */
    private void displayStack() {
        if (rp_NumberStack.size() == 0) {
            System.out.println(Integer.MIN_VALUE);
        }
        else {
            for (int i = 0; i < rp_NumberStack.size(); i++) {
                System.out.println(rp_NumberStack.get(i).intValue());
            }
        }
    }


    /**
     * Function used to check if the stack has enough parameters to perform an action - displays a warning if the Stack
     * would overflow or underflow.
     *
     * @param allowSingleItem - True to allow the command to process if there is only a single item in the stack
     *                        (used for non-operator functions such as '=' or 'd' display).
     * @param isCalculation   - True if the test is for a calculation rather than a number, as calculations are allowed
     *                        to operate if the stack is full as they reduce stack size.
     * @return - Returns True if the stack has space to add a new number and enough items to complete a calculation.
     */
    private Boolean isWithinStackRange(Boolean allowSingleItem, boolean isCalculation) {
        if (rp_NumberStack.size() >= calcStack_MAXSIZE && !isCalculation) {
            System.out.println("Stack overflow.");
            return false;
        }
        else if (rp_NumberStack.size() == 1 && !allowSingleItem) {
            System.out.println("Stack underflow.");
            return false;
        }
        else if (rp_NumberStack.size() == 0 && !allowSingleItem) {
            System.out.println("Stack empty.");
            return false;
        }
        else {
            return true;
        }
    }

    /**
     * assigns the numbers for calculation processing - moved into its own function purely to avoid repeating overly in
     * 'performCalculation' switch statement and so that assignment does not occur prior to checking there are enough
     * items in the array.  The arrangement is not as elegant as I would like but this seems the best compromise to
     * ensure that checks are performed in the same order as the original SRPN calculator and preserve code readability.
     */
    private void assignNums() {
        int topStackIndex = rp_NumberStack.size() - 1;
        firstNum = rp_NumberStack.get(topStackIndex - 1);
        secondNum = rp_NumberStack.get(topStackIndex);
    }

    /* ****************************************************************************************************************** */
    /*
     * FUNCTIONS BELOW THIS POINT ARE USED FOR DEVELOPMENT ONLY AND MAY REQUIRE LINES OF CODE
     * UNCOMMENTING IN THE MAIN SECTION TO WORK
     */


    /**
     * Function Prints the entire stack
     * Deliberately prints the actual double rather than integer value '=' and 'd' use so can see what is going on
     * !!!! Used for troubleshooting during design !!REMOVE ALL REFERENCES PRIOR TO PROD!!
     */
    @SuppressWarnings("unused")
    private void DEBUG_printCalcStack() {
        System.out.println("\n\nStack contents:");
        for (int i = 0; i < rp_NumberStack.size(); i++) {
            System.out.println("rp_NumberStack [" + i + "] = " + rp_NumberStack.get(i));
        }
        System.out.println();
        System.out.println("\nInline Operator Stack contents:");
        for (int i = 0; i < inlineExecutionStack.size(); i++) {
            System.out.println("inlineOperatorStack [" + i + "] = " + inlineExecutionStack.get(i));
        }
        System.out.println();
    }

    /**
     * Function resets the stack by and all values to their starting positions
     * !!! for troubleshooting during design & testing !!REMOVE ALL REFERENCES PRIOR TO PROD!!
     */
    @SuppressWarnings("unused")
    private void DEBUG_Reset_ALL() {
        System.out.println("\n\nClearing stack:");
        rp_NumberStack.removeAllElements();
        System.out.println("Resetting random array pointer to zero:");
        rArray_position = 0;
        System.out.println("Clearing inline operator stack:");
        inlineExecutionStack.removeAllElements();
        System.out.println("Reset finished");
        System.out.println();
    }


    /**
     * Function Prints the entire InlineExecutionStack
     * !!!! Used for troubleshooting during design !!REMOVE ALL REFERENCES PRIOR TO PROD!!
     */
    @SuppressWarnings("unused")
    private void DEBUG_printInlineExecutionStack() {
        System.out.println("\nInline Operator Stack contents:");
        for (int i = 0; i < inlineExecutionStack.size(); i++) {
            System.out.println("inlineOperatorStack [" + i + "] = " + inlineExecutionStack.get(i));
        }
        System.out.println();
    }

}
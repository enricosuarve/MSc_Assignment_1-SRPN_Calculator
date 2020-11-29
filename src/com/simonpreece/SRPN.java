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
 * # Displays warning "Stack overflow." when trying to put an entry on the stack beyond the maximum stack size; this discards any items added but does not reset the stack or prevent further actions which would not increase stack size.
 * # Displays warning "Stack underflow." when trying to perform operations without enough entries (2) on the stack this discards any actions added but does not reset the stack or prevent further actions.
 * # Entering equals '=' output the value held at the top of the stack.
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
    // rArray holds the values for the pseudo-random function 'r'.
    private final double[] rArray = new double[]{1804289383, 846930886, 1681692777, 1714636915, 1957747793, 424238335,
            719885386, 1649760492, 596516649, 1189641421, 1025202362, 1350490027, 783368690, 1102520059, 2044897763,
            1967513926, 1365180540, 1540383426, 304089172, 1303455736, 35005211, 521595368};
    boolean inCommentMode = false; // Used to decide whether to process commands entered or ignore.
    private int rArray_position = 0;
    private double firstNum, secondNum;
    private boolean debugMode = false;

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
        double currentNum;
        int currentTopOfStack;
        double currentNumInChar;
        double currentNumToStore = 0;
        char currentChar;
        boolean isNumToStore = false;
        boolean lastCharWasOperator;
        boolean minusReceivedAfterOtherOperator;
        boolean isFirstChar = true; // End of commandBlock execution is skipped if first commandBlock is just an operator.
        boolean lastCharWasMinus ;
        boolean twoMinusInARow ;
        if (s.length() > 0) { // Ignore empty lines as nothing to do.
            do {
                currentTopOfStack = rp_NumberStack.size() - 1;
                commandBlock = CommandScanner.next(); // Execute each set of instructions between whitespace separately.
                lastCharWasOperator = false; // Reset for each block as they appear to process separately.
                minusReceivedAfterOtherOperator = false;
                lastCharWasMinus = false;
                twoMinusInARow = false;
                if (debugMode) {
                    System.out.println("processing commandBlock '" + commandBlock + "'");
                }
                try {
                    // Checks if is a long and throws an Exception if not.
                    currentNum = Long.parseLong(commandBlock);
                    if (!inCommentMode) {
                        /* Add to stack if there is space and the number does NOT start with a '+' (for parseLong this
                            means a number is positive, but not in the original SRPN, where it means 'add results to
                            current top of stack'. */
                        if (commandBlock.charAt(0) == '+') {
                            if (debugMode) {
                                System.out.printf("received '+%f' so adding to number at top of stack\n", currentNum);
                            }
                            rp_NumberStack.set(currentTopOfStack,
                                    saturateNumber(rp_NumberStack.get(currentTopOfStack) + currentNum));
                        }
                        else if (isWithinStackRange(true, false)) {
                            if (debugMode) {
                                System.out.printf("received '%f' so adding to number stack\n", currentNum);
                            }
                            rp_NumberStack.push(saturateNumber(currentNum));
                        }
                        lastCharWasOperator = false;
                    }
                    else {
                        if (debugMode) {
                            System.out.printf("in comment mode so ignoring '%f'\n", currentNum);
                        }
                        // Do nothing as currently in comment mode.
                    }
                } catch (Exception e) {
                    // Command block is a non-integer string or a mixture of text and numbers.
                    currentChar = commandBlock.charAt(0);
                    if (debugMode) {
                        //System.out.printf("parseLong '%s' generated an exception '%s'\n", commandBlock, e.toString());
                        System.out.printf("command block '%s' contained a non integer - processing\n", commandBlock);
                    }
                    if (commandBlock.length() == 1 && currentChar == '#') { /* CommentMode on/off is ONLY triggered if
                                                                                  the '#' is on its own in a block. */
                        inCommentMode = !inCommentMode;
                        if (debugMode) {
                            System.out.println("received '#' comment mode - " + (inCommentMode ? "On" : "Off"));
                        }
                    }
                    else {
                        if (debugMode) {
                            System.out.printf("'%s' did not match '#' inCommentMode = %b\n", commandBlock, inCommentMode);
                        }
                        // Loop through characters in commandBlock.
                        for (int i = 0; i < commandBlock.length(); i++) {
                            currentChar = commandBlock.charAt(i);
                            if (!inCommentMode) {
                                if (debugMode) {
                                    System.out.printf("checking char '%c'\n", currentChar);
                                }
                                try {
                                    currentNumInChar = Double.parseDouble(String.valueOf(currentChar));
                                    if (debugMode) {
                                        System.out.printf("char '%c' is a number\n", currentChar);
                                    }
                                    isNumToStore = true;
                                    // Add the current char number to the running total to add to stack later.
                                    currentNumToStore = currentNumToStore * 10 + currentNumInChar;
                                    lastCharWasOperator = false;
                                } catch (Exception ee) { // The character received is not a number.
                                    if (debugMode) {
                                        //System.out.printf("char '%c' is NOT a number\n", currentChar);
                                    }

                                    /* Check if currentChar has a BODMAS priority and is therefore a valid operator
                                                                                         - display warning if not. */
                                    if (bodmasPriority(currentChar) == 0) {
                                        if (debugMode) {
                                            System.out.printf("currentChar '%c' has no Bodmas so unrecognised at this point - raise warning and do not add to stack\n", currentChar);
                                        }
                                        System.out.println("Unrecognised operator or operand \"" + currentChar + "\".");
                                    }
                                    else {
                                        if (debugMode) {
                                            System.out.println("Character is a valid operator");
                                        }
                                        if (isNumToStore) { /* Check if have already previously been passed a number, if so
                                                                add it to the stack and reset NumToStore variables. */
                                            if (isWithinStackRange(true, false)) {
                                                if (twoMinusInARow) {
                                                    if (minusReceivedAfterOtherOperator) {
                                                        if (debugMode) {
                                                            System.out.printf("Last char/s in commandBlock made a number but was twoMinusInARow && minusReceivedAfterOtherOperator- adding %f to stack\n", currentNumToStore);
                                                        }
                                                        rp_NumberStack.push(currentNumToStore);
                                                    }
                                                    else {
                                                        if (debugMode) {
                                                            System.out.printf("Last char/s in commandBlock made a number and was twoMinusInARow but not minusReceivedAfterOtherOperator - adding %f to stack\n", currentNumToStore * -1);
                                                        }
                                                        rp_NumberStack.push(currentNumToStore * -1);
                                                        if (debugMode) {
                                                            System.out.println("popping the last minus from the stack as it was a negative number sign");
                                                        }
                                                        inlineExecutionStack.pop();
                                                    }
                                                }
                                                else {
                                                    if (debugMode) {
                                                        System.out.printf("Last char/s in commandBlock made a number and was not twoMinusInARow - adding %f to stack\n", currentNumToStore);
                                                    }
                                                    rp_NumberStack.push(currentNumToStore);

                                                }
                                                /*
                                                if (debugMode) {
                                                    System.out.printf("For loop received operator but previous char/s made a number - adding %f to stack\n"
                                                            , currentNumToStore * (minusReceivedAfterOtherOperator ? -1 : 1));
                                                }
                                                // Push number to stack with correct polarity.
                                                rp_NumberStack.push(currentNumToStore
                                                        * (minusReceivedAfterOtherOperator ? -1 : 1));
*/
                                            }
                                            else {
                                                if (debugMode) {
                                                    System.out.printf("failed to add %f to stack\n", currentNumToStore);
                                                }
                                            }
                                            if (minusReceivedAfterOtherOperator) {
                                                inlineExecutionStack.pop(); /* Get rid of the minus from the top of the stack
                                                                            as it was a number sign not an operator. */
                                                minusReceivedAfterOtherOperator = false;
                                                lastCharWasMinus = false;
                                                if (debugMode) {
                                                    System.out.println("processing the number using the character for-loop block");
                                                }

                                            }
                                            /* The original 'loses' the number at this point if it can't save it, so leave
                                                                                    following outside the check. */
                                            if (debugMode) {
                                                System.out.println("number placed in stack setting twoMinusInARow = false");
                                            }
                                            twoMinusInARow = false;
                                            currentNumToStore = 0;
                                            isNumToStore = false;
                                        }// - End of NumStore

                                        twoMinusInARow =  (lastCharWasMinus && currentChar == '-');
                                        if (debugMode) {
                                            System.out.printf("checked lastCharWasMinus && currentChar =='-' setting twoMinusInARow = %b\n", twoMinusInARow);
                                        }

                                        if (minusReceivedAfterOtherOperator) {
                                            if (currentChar == '-') {/* two minuses in a row after another operator - definitely
                                             execute inlineExecutionStack
                                              original seems to convert the current char to a plus at this point*/
                                                //   currentChar = '+'; // poss so this at some point???+
                                                if (debugMode) {
                                                    System.out.println("minusreceivedafterotheropchek Operator other than '-' received setting twoMinusInARow = true");
                                                }

                                                executeInlineExecutionStack(true, false);
                                            }
                                            else { /* the previous minus
                                            did not turn out to be a number sign - check if the stack should have been
                                            executed and do so (compare BODMAS of previous two chars). */

                                                if (debugMode) {
                                                    System.out.println("Previous character was a minus but following was not a" +
                                                            " number - checking if should have executed stack");
                                                }
                                                if (inlineExecutionStack.size()>1 && bodmasPriority(inlineExecutionStack.get(inlineExecutionStack.size() - 2)) >
                                                        bodmasPriority(inlineExecutionStack.peek())) {
                                                    if (debugMode) {
                                                        System.out.printf("Operator '%c'(%d) > '%c'(%d) - executing stack\n",
                                                                inlineExecutionStack.get(inlineExecutionStack.size() - 2),
                                                                bodmasPriority(inlineExecutionStack.get(inlineExecutionStack.size() - 2)),
                                                                inlineExecutionStack.peek(),
                                                                bodmasPriority(inlineExecutionStack.peek()));
                                                    }
                                                    executeInlineExecutionStack(true, false);
                                                }
                                                else {
                                                   /* if (debugMode) {
                                                        System.out.printf("Operator '%c'(%d) !> '%c'(%d) - leaving stack in peace\n",
                                                                inlineExecutionStack.get(inlineExecutionStack.size() - 2),
                                                                bodmasPriority(inlineExecutionStack.get(inlineExecutionStack.size() - 2)),
                                                                inlineExecutionStack.peek(),
                                                                bodmasPriority(inlineExecutionStack.peek()));
                                                    }*/
                                                }
                                                minusReceivedAfterOtherOperator = false;
                                            }
                                        }// - end of minusReceivedAfterOtherOperator check

                                        if (lastCharWasOperator && !lastCharWasMinus && currentChar == '-') { /* If a Minus is received after
                                            another operator it could be a negative number sign, do nothing for now
                                            but note that the condition has happened. */
                                            if (debugMode) {
                                                System.out.println("lastCharWasOperator && currentChar == '-'; setting minusReceivedAfterOtherOperator = true;");
                                            }
                                            minusReceivedAfterOtherOperator = true;
                                        }
                                        if (isFirstChar && currentChar == '-') {
                                            minusReceivedAfterOtherOperator = true; /* Original treats minuses at the start of a commandBlock as if they were following another operator */
                                            if (debugMode) {
                                                System.out.println("isFirstChar && currentChar =='-'; setting minusReceivedAfterOtherOperator = true");
                                            }
                                        }

                                        if (currentChar == 'd') {
                                            executeInlineExecutionStack(false, false); /* In original 'd' triggers an
                                                execution of the stack so far and displays the stack. */
                                            if (debugMode) {
                                                System.out.println("'d' RECEIVED = DISPLAYING");
                                            }
                                            displayStack();
                                        }
                                        if (currentChar == '=') {
                                        /* command has received an equals - in the original this executes immediately,
                                            hence checking for it here. */
                                            if (debugMode) {
                                                System.out.printf("Executing '%c'\n", currentChar);
                                                DEBUG_printCalcStack();
                                            }
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
                                        if (!twoMinusInARow &&
                                                !inlineExecutionStack.isEmpty() &&
                                                bodmasPriority(currentChar) > 0 &&
                                                currentChar != '=' &&
                                                bodmasPriority(inlineExecutionStack.peek()) > bodmasPriority(currentChar)) {
                                            if (debugMode) {
                                                System.out.printf("Operator '%c' is > to operator '%c' - executing stack\n",
                                                        inlineExecutionStack.peek(), currentChar);
                                            }
                                            executeInlineExecutionStack(false, false);
                                        }
                                        else {
                                            if (debugMode) {
                                                char lastOperatorInStack = (!inlineExecutionStack.isEmpty() ?
                                                        inlineExecutionStack.peek() : ' ');
                                                System.out.printf("Either the previous operator '%c' was a lower priority (%d)" +
                                                        "than '%c'(%d), this is an invalid char or = or this is this is the only" +
                                                        " one in the stack\n", lastOperatorInStack, bodmasPriority(lastOperatorInStack),currentChar, bodmasPriority(currentChar));
                                            }
                                        }
                                        if (debugMode) {
                                            System.out.printf("adding operator '%c' to command stack\n", currentChar);
                                        }

                                        lastCharWasMinus = (currentChar == '-');
                                        if (debugMode) {
                                            System.out.printf("set lastCharWasMinus = %b\n", lastCharWasMinus);
                                        }
                                        inlineExecutionStack.push(currentChar);
                                        lastCharWasOperator = true;

                                    }//- end of Bodmas Operator Validity Check
                                }
                            }
                            else {
                                // Do nothing as currently in comment mode.
                                if (debugMode) {
                                    System.out.printf("in comment mode - ignoring '%c'\n", currentChar);
                                }
                            }
                            isFirstChar = false;

                        }//--end of single character loop.
                    }//--end of check for comment mode.

                    //execute any valid command at the end of a block.
                    if (bodmasPriority(currentChar) > 0 && bodmasPriority(currentChar) <= 6 && !inCommentMode) {
                        if (debugMode) {
                            System.out.printf("'%c' operator received at the end of a CommandBlock - executing '%c'\n",
                                    currentChar, currentChar);
                        }
                        performRP_Calculation(inlineExecutionStack.pop());
                    }
                }// -- End of commandBlock number check exception.

                // If the commandBlock contained more than one character process the instructions.
                if (!isFirstChar) {
                    if (debugMode) {
                        System.out.println("got to the end of a commandBlock - executing");
                    }
                    // Check if have already been passed a number, add it to the stack and reset NumToStore variables.
                    if (isNumToStore) {
                        if (isWithinStackRange(true, false)) {
                            if (debugMode) {
                                System.out.printf("Last char/s in commandBlock  made a number - adding %f to stack\n", currentNumToStore * (minusReceivedAfterOtherOperator ? -1 : 1));
                            }
                            /* The original SRPN has a 'feature' whereby it does not appear to check number polarity at the end of
                                a commandBlock (e.g. 5, 10, --50*/
                            if (twoMinusInARow) {
                                if (minusReceivedAfterOtherOperator) {
                                    if (debugMode) {
                                        System.out.printf("Last char/s in commandBlock made a number but was twoMinusInARow && minusReceivedAfterOtherOperator- adding %f to stack\n", currentNumToStore);
                                    }
                                    rp_NumberStack.push(currentNumToStore);
                                }
                                else {
                                    if (debugMode) {
                                        System.out.printf("Last char/s in commandBlock made a number and was twoMinusInARow but not minusReceivedAfterOtherOperator - adding %f to stack\n", currentNumToStore * -1);
                                    }
                                    rp_NumberStack.push(currentNumToStore * -1);
                                    if (debugMode) {
                                        System.out.println("popping the last minus from the stack as it was a negative number sign");
                                    }
                                    inlineExecutionStack.pop();
                                }
                            }
                            else {
                                if (debugMode) {
                                    System.out.printf("Last char/s in commandBlock made a number and was not twoMinusInARow - adding %f to stack\n", currentNumToStore);
                                }
                                rp_NumberStack.push(currentNumToStore);

                            }

                        }
                        else {
                            if (debugMode) {
                                System.out.printf("failed to add %f to stack\n", currentNumToStore);
                            }
                        }
                        if (minusReceivedAfterOtherOperator && !twoMinusInARow) {
                            inlineExecutionStack.pop(); //get rid of the minus from the top of the stack as it was a number sign not an operator
                        }
                        currentNumToStore = 0;
                        isNumToStore = false;
                    }
                    // Execute the entire command block as have reached its end.
                    executeInlineExecutionStack(false, false);
                    if (debugMode) {
                        System.out.println("Finished the 'end of a commandBlock' execution");
                    }
                }
                else {
                    if (debugMode) {
                        System.out.println("'End of a commandBlock' execution ignored as this is the first character");
                    }
                }
            } while (CommandScanner.hasNext()); //--if string has another commandBlock, loop back and do again
            if (debugMode) {
                System.out.printf("While loop finished - inlineExecutionStack.size() = %d\n", inlineExecutionStack.size());
            }
            if (debugMode) {
                System.out.printf("Finished executing inlineExecutionStack : inlineExecutionStack.size() = %d\n", inlineExecutionStack.size());
            }
        }

    }


    /**
     * Execute all instructions in the Execution Stack.
     *
     * @param skipTopInstruction - If 'true' execution starts one from the top (used for retrospectively executing the
     *                           stack if a minus sign turned out to be an operator and not a negative number sign).
     */
    private void executeInlineExecutionStack(boolean skipTopInstruction, boolean executeInReverseOrder) {
        char TopInstruction = 0;
        if (executeInReverseOrder) {
            while (inlineExecutionStack.size() > 0) {
                performRP_Calculation(inlineExecutionStack.remove(0));
            }
        }
        else {
            if (skipTopInstruction) {
                if (debugMode) {
                    System.out.print("Skipping Top Instruction in stack");
                }
                TopInstruction = inlineExecutionStack.pop();
            }
            while (inlineExecutionStack.size() > 0) {
                if (debugMode) {
                    System.out.printf("Executing command '%c'\n", inlineExecutionStack.peek());
                }
                performRP_Calculation(inlineExecutionStack.pop());
            }
            if (skipTopInstruction) {
                inlineExecutionStack.push(TopInstruction); //put the instruction skipped back on the top of the stack.
            }
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
            case 'p':  //!!!!REMOVE BEFORE GOING TO PRODUCTION!!!!
                priority++;
            case 'b':  //!!!!REMOVE BEFORE GOING TO PRODUCTION!!!!
                priority++;
            case 'c':  //!!!!REMOVE BEFORE GOING TO PRODUCTION!!!!
                priority++;
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
            case 'c': //!!!!REMOVE BEFORE GOING TO PRODUCTION!!!!
                DEBUG_Reset_ALL();
                break;
            case 'p':  //!!!!REMOVE BEFORE GOING TO PRODUCTION!!!!
                DEBUG_printCalcStack();
                break;
            case 'b':  //!!!!REMOVE BEFORE GOING TO PRODUCTION!!!!
                debugMode = !debugMode;
                break;
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
     * Function Prints the entire stack
     * Deliberately prints the actual double rather than integer value '=' and 'd' use so can see what is going on
     * !!!! Used for troubleshooting during design !!REMOVE ALL REFERENCES PRIOR TO PROD!!
     */
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
     * Function Prints the entire InlineExecutionStack
     * !!!! Used for troubleshooting during design !!REMOVE ALL REFERENCES PRIOR TO PROD!!
     */
    private void DEBUG_printInlineExecutionStack() {
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
     * Display the current stack on a new line per entry with no other formatting.
     */
    private void displayStack() {
        for (int i = 0; i < rp_NumberStack.size(); i++) {
            System.out.println(rp_NumberStack.get(i).intValue());
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
}
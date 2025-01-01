import java.util.Map;
import java.util.HashMap;
import java.util.Scanner;

public class PythonInterpreter {
    public final Map<String, Integer> variables = new HashMap<>();
    private final Map<String, Boolean> booleanVariables = new HashMap<>();
    private final Scanner scanner = new Scanner(System.in);

    public void eval(String code) {
        String[] lines = code.split("\\r?\\n");
        executeStatements(lines, 0, 0);
    }

    private int executeStatements(String[] lines, int startIndex, int indentLevel) {
        int index = startIndex;
        while (index < lines.length) {
            String line = lines[index].trim();

            if (line.isEmpty() || line.startsWith("#")) {
                index++;
                continue;
            }

            int currentIndent = getIndentLevel(lines[index]);
            if (currentIndent < indentLevel) {
                return index - 1;
            }

            if (line.contains("=") && !isComparisonOperator(line)) {
                if (line.contains("input")) {
                    handleInput(line);
                } else {
                    handleAssignment(line);
                }
            } else if (line.startsWith("for")) {
                index = handleForLoop(lines, index);
            } else if (line.startsWith("while")) {
                index = handleWhileLoop(lines, index);
            } else if (line.startsWith("if")) {
                index = handleIfStatement(lines, index);
            } else if (line.startsWith("print")) {
                handlePrint(line);
            } else {
                System.out.println("Unknown statement: " + line);
            }
            index++;
        }
        return index;
    }

    private boolean isComparisonOperator(String line) {
        return line.contains("==") || line.contains("!=") ||
                line.contains(">=") || line.contains("<=") ||
                line.matches(".*[<>].*");
    }

    private int getIndentLevel(String line) {
        int spaces = 0;
        while (spaces < line.length() && line.charAt(spaces) == ' ') {
            spaces++;
        }
        return spaces / 4;
    }

    private void handleInput(String line) {
        String varName = line.substring(0, line.indexOf("=")).trim();
        String inputPart = line.substring(line.indexOf("input")).trim();
        String prompt = inputPart.substring(inputPart.indexOf("(") + 1, inputPart.indexOf(")")).trim();

        if (!prompt.isEmpty()) {
            if (prompt.startsWith("\"") && prompt.endsWith("\"")) {
                System.out.print(prompt.substring(1, prompt.length() - 1) + " ");
            }
        }

        try {
            int value = Integer.parseInt(scanner.nextLine());
            variables.put(varName, value);
        } catch (NumberFormatException e) {
            System.out.println("Invalid input. Please enter a number.");
            variables.put(varName, 0);
        }
    }

    private void handleAssignment(String line) {
        String[] parts = line.split("=");
        String varName = parts[0].trim();
        String value = parts[1].trim().toLowerCase();

        if (value.equals("true")) {
            booleanVariables.put(varName, true);
            variables.put(varName, 1);
        } else if (value.equals("false")) {
            booleanVariables.put(varName, false);
            variables.put(varName, 0);
        } else {
            variables.put(varName, evaluateExpression(value));
            booleanVariables.remove(varName);
        }
    }

    private int handleForLoop(String[] lines, int index) {
        String line = lines[index];
        String loopHeader = line.substring(line.indexOf("for") + 3, line.indexOf(":")).trim();
        String[] loopParts = loopHeader.split("in");
        String loopVar = loopParts[0].trim();
        String rangeExpression = loopParts[1].trim().replace("range(", "").replace(")", "");
        String[] rangeBounds = rangeExpression.split(",");

        int start = evaluateExpression(rangeBounds[0].trim());
        int end = evaluateExpression(rangeBounds[1].trim());

        int blockEnd = findBlockEnd(lines, index + 1, getIndentLevel(lines[index + 1]));

        for (int i = start; i < end; i++) {
            variables.put(loopVar, i);
            int result = executeStatements(lines, index + 1, getIndentLevel(lines[index + 1]));
            if (result == -2) break;
        }

        return blockEnd;
    }

    private int handleWhileLoop(String[] lines, int index) {
        String line = lines[index];
        String condition = line.substring(line.indexOf("while") + 5, line.indexOf(":")).trim();

        int blockEnd = findBlockEnd(lines, index + 1, getIndentLevel(lines[index + 1]));

        while (evaluateCondition(condition)) {
            int result = executeStatements(lines, index + 1, getIndentLevel(lines[index + 1]));
            if (result == -2) break;
        }

        return blockEnd;
    }

    private int handleIfStatement(String[] lines, int index) {
        String line = lines[index];
        String condition = line.substring(line.indexOf("if") + 2, line.indexOf(":")).trim();
        boolean conditionResult = evaluateCondition(condition);

        int currentIndex = index;
        int currentIndentLevel = getIndentLevel(lines[index + 1]);

        if (conditionResult) {
            currentIndex = executeStatements(lines, index + 1, currentIndentLevel);
            currentIndex = skipElseBlock(lines, currentIndex + 1);
        } else {
            currentIndex = findBlockEnd(lines, index + 1, currentIndentLevel);
            if (currentIndex + 1 < lines.length && lines[currentIndex + 1].trim().equals("else:")) {
                currentIndex = executeStatements(lines, currentIndex + 2, currentIndentLevel);
            }
        }

        return currentIndex;
    }

    private int skipElseBlock(String[] lines, int startIndex) {
        if (startIndex < lines.length && lines[startIndex].trim().equals("else:")) {
            return findBlockEnd(lines, startIndex + 1, getIndentLevel(lines[startIndex + 1]));
        }
        return startIndex - 1;
    }

    private int findBlockEnd(String[] lines, int startIndex, int blockIndentLevel) {
        int i = startIndex;
        while (i < lines.length) {
            String line = lines[i].trim();
            if (line.isEmpty() || line.startsWith("#")) {
                i++;
                continue;
            }
            if (getIndentLevel(lines[i]) < blockIndentLevel) {
                return i - 1;
            }
            i++;
        }
        return i - 1;
    }

    private void handlePrint(String line) {
        String content = line.substring(line.indexOf('(') + 1, line.indexOf(')')).trim();
        if (content.startsWith("\"") && content.endsWith("\"")) {
            System.out.println(content.substring(1, content.length() - 1));
        } else if (booleanVariables.containsKey(content)) {
            System.out.println(booleanVariables.get(content));
        } else {
            System.out.println(evaluateExpression(content));
        }
    }

    private boolean evaluateCondition(String condition) {
        condition = condition.trim();

        if (booleanVariables.containsKey(condition)) {
            return booleanVariables.get(condition);
        }

        if (condition.equals("true")) return true;
        if (condition.equals("false")) return false;

        if (condition.contains("<=")) {
            String[] parts = condition.split("<=");
            return evaluateExpression(parts[0].trim()) <= evaluateExpression(parts[1].trim());
        } else if (condition.contains(">=")) {
            String[] parts = condition.split(">=");
            return evaluateExpression(parts[0].trim()) >= evaluateExpression(parts[1].trim());
        } else if (condition.contains("==")) {
            String[] parts = condition.split("==");
            return evaluateExpression(parts[0].trim()) == evaluateExpression(parts[1].trim());
        } else if (condition.contains("!=")) {
            String[] parts = condition.split("!=");
            return evaluateExpression(parts[0].trim()) != evaluateExpression(parts[1].trim());
        } else if (condition.contains("<")) {
            String[] parts = condition.split("<");
            return evaluateExpression(parts[0].trim()) < evaluateExpression(parts[1].trim());
        } else if (condition.contains(">")) {
            String[] parts = condition.split(">");
            return evaluateExpression(parts[0].trim()) > evaluateExpression(parts[1].trim());
        }
        return evaluateExpression(condition) != 0;
    }

    private int evaluateExpression(String expression) {
        expression = expression.trim();
        if (expression.contains("+")) {
            String[] parts = expression.split("\\+");
            int result = 0;
            for (String part : parts) {
                result += evaluateExpression(part.trim());
            }
            return result;
        } else if (expression.contains("-")) {
            String[] parts = expression.split("-");
            if (parts.length == 1) {
                return -evaluateExpression(parts[0].trim());
            }
            return evaluateExpression(parts[0].trim()) - evaluateExpression(parts[1].trim());
        } else if (expression.contains("*")) {
            String[] parts = expression.split("\\*");
            return evaluateExpression(parts[0].trim()) * evaluateExpression(parts[1].trim());
        } else if (expression.contains("/")) {
            String[] parts = expression.split("/");
            int divisor = evaluateExpression(parts[1].trim());
            if (divisor == 0) {
                throw new ArithmeticException("Division by zero is not allowed.");
            }
            return evaluateExpression(parts[0].trim()) / divisor;
        } else if (expression.contains("%")) {
            String[] parts = expression.split("%");
            return evaluateExpression(parts[0].trim()) % evaluateExpression(parts[1].trim());
        }

        try {
            return Integer.parseInt(expression);
        } catch (NumberFormatException e) {
            return variables.getOrDefault(expression, 0);
        }
    }

    public static void main(String[] args) {
        PythonInterpreter interpreter = new PythonInterpreter();

        String[] programs = new String[]{
                // Program 1: Sum of numbers
                """
            n = input("Enter number")
            sum = 0
            for i in range(1, n + 1):
                sum = sum + i
            print(sum)
            """,

                // Program 2: Factorial
                """
            n = 5
            factorial = 1
            for i in range(1, n + 1):
                factorial = factorial * i
            print(factorial)
            """,

                // Program 3: GCD
                """
            a = 15
            b = 18
            while b != 0:
                t = b
                b = a % b
                a = t
            print(a)
            """,

                // Program 4: Reverse number
                """
            x = 123456789
            r = 0
            while x > 0:
                d = x % 10
                r = r * 10 + d
                x = x / 10
            print(r)
            """,

                // Program 5: Palindrome check
                """
            x = 123321
            o = x
            r = 0
            while x > 0:
                d = x % 10
                r = r * 10 + d
                x = x / 10
            if o == r:
                print("palindrome")
            else:
                print("not palindrome")
            """,

                // Program 6: Fibonacci
                """
            n = 8
            a = 0
            b = 1
            i = 2
            while i < n + 1:
                c = a + b
                a = b
                b = c
                i = i + 1
            if n == 0:
                print(0)
            if n == 1:
                print(1)    
            else:
                print(b)
            """,

                // Program 7: Sum of digits
                """
            x = 2929
            s = 0
            while x > 0:
                d = x % 10
                s = s + d
                x = x / 10
            print(s)
            """,

                // Program 8: Multiplication table
                """
            n = 5
            i = 1
            while i < 10:
                print(n * i)
                i = i + 1
            """,

                // Program 9: Prime check
                """
            number = input("Enter number")
            prime = true 
            if number <= 1:
                prime = true
            else:
                for i in range(2, number):
                    if number % i == 0:
                        prime = false 
            if prime:
                print("It's prime")
            else:
                print("It's not prime")
            """,

                // Program 10: Largest digit
                """
            x = input("Enter number");
            m = 0
            while x > 0:
                d = x % 10
                if d > m:
                    m = d
                x = x / 10
            print(m)
            """

        };

        // Run all test programs
        for (int i = 0; i < programs.length; i++) {
            System.out.println("\nRunning Program " + (i + 1) + ":");
            interpreter.variables.clear();
            interpreter.booleanVariables.clear();
            interpreter.eval(programs[i]);
        }
    }
}
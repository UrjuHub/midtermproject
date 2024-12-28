import java.util.HashMap;
import java.util.Map;


public class PythonInterpreter {
    public final Map<String, Integer> variables = new HashMap<>(); // Variable storage


    public void eval(String code) {
        String[] lines = code.split("\\r?\\n");  // Split by line breaks
        int index = 0;
        while (index < lines.length) {
            String line = lines[index].trim();
            if (line.isEmpty()) {
                index++;
                continue;
            }

            // Handle variable assignment
            if (line.contains("=")) {
                handleAssignment(line);
            }

            // Handle for loop
            else if (line.startsWith("for")) {
                index = handleForLoop(lines, index);
            }

            // Handle print statements
            else if (line.startsWith("print")) {
                handlePrint(line);
            } else {
                System.out.println("Unknown line: " + line);
            }
            index++;
        }
    }

    private void handleAssignment(String line) {
        String[] parts = line.split("=");
        String varName = parts[0].trim();
        String expression = parts[1].trim();
        int value = evaluateExpression(expression);
        variables.put(varName, value);
    }

    private void handlePrint(String line) {
        String varName = line.substring(line.indexOf('(') + 1, line.indexOf(')')).trim();

        if (varName.contains("+") || varName.contains("-") ||
                varName.contains("*") || varName.contains("/") || varName.contains("%")) {
            System.out.println(evaluateExpression(varName));
        } else {
            // If there is some String message
            if (varName.charAt(0) == '"' && varName.charAt(varName.length() - 1) == '"') {
                varName = varName.substring(1, varName.length() - 1);
                System.out.println(varName);

            } else {
                System.out.println(variables.getOrDefault(varName, 0));
            }


        }
    }

    private int handleForLoop(String[] lines, int index) {
        String line = lines[index];
        // Parse for loop syntax: for i in range(start, end)
        String loopHeader = line.substring(line.indexOf("for") + 3, line.indexOf(":")).trim();
        String[] loopParts = loopHeader.split("in");
        String loopVar = loopParts[0].trim();
        String rangeExpression = loopParts[1].trim().replace("range(", "").replace(")", "");
        String[] rangeBounds = rangeExpression.split(",");

        // Evaluate range bounds
        int start = evaluateExpression(rangeBounds[0].trim());
        int end = evaluateExpression(rangeBounds[1].trim());

        // Collect loop body
        int loopStart = index + 1;
        StringBuilder loopBody = new StringBuilder();
        // Loop body is written after one tab
        while (++index < lines.length && lines[index].startsWith("    ")) {
            loopBody.append(lines[index].trim()).append("\n");
        }
        index--; // Step back since we overshot the loop body

        // Execute the loop
        for (int i = start; i < end; i++) {
            variables.put(loopVar, i);
            executeLoopBody(loopBody.toString());
        }
        return index;
    }

    private void executeLoopBody(String loopBody) {
        String[] statements = loopBody.split("\n");
        for (String statement : statements) {
            if (statement.contains("=")) {
                handleAssignment(statement);
            } else if (statement.startsWith("print")) {
                handlePrint(statement);
            } else {
                throw new IllegalArgumentException("Unrecognized statement in loop body: " + statement);
            }
        }
    }


    private int evaluateExpression(String expression) {
        // Handle addition, subtraction, multiplication, division, and modulus
        if (expression.contains("+")) {
            String[] parts = expression.split("\\+");
            int result = 0;
            for (String part : parts) {
                result += evaluateExpression(part.trim());
            }
            return result;
        } else if (expression.contains("-")) {
            String[] parts = expression.split("-");
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
        } else {
            if (variables.containsKey(expression)) {
                return variables.get(expression);
            } else {
                return Integer.parseInt(expression); // Parse as a number if it's not a variable
            }
        }
    }

    public static void main(String[] args) {
        PythonInterpreter interpreter = new PythonInterpreter();

        String program = """
                n = 10
                sum = 0
                a = 17%5
                sum1 = 0
                
                for i in range(1, n + 1):
                    sum1 = sum1 + a * i
                    sum = sum + i
                    sum = sum + sum1
                    print(sum)
                
                print("Total sum")
                print(sum)
                print(sum1)
                
                """;

        interpreter.eval(program);
    }
}
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
        // If there is some String message
        if (varName.charAt(0) == '"' && varName.charAt(varName.length() - 1) == '"') {
            varName = varName.substring(1, varName.length() - 1);
            System.out.println(varName);
        }

        if (varName.contains("+") || varName.contains("-") ||
                varName.contains("*") || varName.contains("/") || varName.contains("%")) {
            System.out.println(evaluateExpression(varName));
        } else {
            System.out.println(variables.getOrDefault(varName, 0));
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
                n = 5
                m = 1
                a = n * m
                b = n / m
                
                print(a+b)
                print("hello")
                
                """;

        interpreter.eval(program);
    }
}
On first commit I extend evaluateExpression method so it can handle all basic operations +,-,*,/,%;
also i extend handlePrint method so it can print Strings too;
on the second commit i add handleForLoop and executeLoopBody methods, 
first method handles the parsing and execution, it evaluates the start and end values of the range using the
evaluateExspression method and collects loop body then runs it, and returns updates index in lines array;
we also have executeLoopBody which checks if line is assigment or print method and calls appropriate functions;

On first commit I extend evaluateExpression method so it can handle all basic operations +,-,*,/,%;
also I extend handlePrint method so it can print Strings too;

On the second commit I add handleForLoop and executeLoopBody methods, 
first method handles the parsing and execution, it evaluates the start and end values of the range using the
evaluateExspression method and collects loop body then runs it, and returns updates index in lines array;
we also have executeLoopBody which checks if line is assigment or print method and calls appropriate functions;

On the third commit I add methods to handle everything(loops, if-else, conditions), but code was struggling when 
there was if-else statements under for/while. also in some cases conditions such as: <=, >=, ==, !=;  while running
program was seeing it as assigment. 

On the fourth commit I extend program with some helper methods, so it can handle every required functions test and,
it fixed problems in third commit. 

In last commit I extend program so it can handle boolean variables and input. I tested this code for many functions 
and works great.



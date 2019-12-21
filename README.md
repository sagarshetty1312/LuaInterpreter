# LuaInterpreter
This project is an interpreter for a subset of the Lua language. This was a course assignment for Programming Language Principles at University of Florida under Dr. Beverly A Sanders. Support for for loops, scope for variables and float data type needs to be added.
The design is a two pass compiler, where the first pass is a static one which creates code points for break statements taking into account the scoping rules for the Lua language.
In second pass, the parser first creates a Abstract Syntax Tree with all the tokens and then visits the AST to run programs.

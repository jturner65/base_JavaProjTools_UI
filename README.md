# base_UIProjectJTools
UI, IO, math, vector and matrix utilities I use in all my java projects

These packages contain code I wrote that I use in nearly all of my java projects.  Functionality I have written governing openGL access, window and UI object definition and building, file and screen IO, drawn curves and splines, and point, vector and matrix access are included here so that I don't have to import all these files repeatedly into every project I write, using the jar of this project instead;  the compiled jar of this project should always include the source code for easy reference.  

The immediate plans for the functionality this project covers are : 
--- Expand the IRenderInterface to describe the complete expectation of rendering functionality that any project using these libraries should meet
--- Add generalized multi-threaded loading/saving abstract runner/callable base class structures from ML projects
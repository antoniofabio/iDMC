--%  ft| TRAJECTORY_T0_V0_A1_O1 sn| Slow~convergence~to~a~fixed~point n| #0 d| 0.01 n| #1 d| 0.02 n| #2 d| -7e+4 n| #3 d| 0.05 n| #4 d| 0 n| #5 d| 75000 n| #6 d| 75000 n| #7 d| x n| #8 d| y 
--@@
name = "Conlyapb"
description = " See Model refs in user's guide)"
type = "C"
parameters = {"k"}
variables = {"x", "y"}

function f (k, x, y)

	x1 =  y + k * x * (x^2 + y^2)
	
	y1 = - x 

	return x1, y1

end

function Jf(k, x, y)

	return   
		
	3 * k * x^2	+ k * y^2,	 2 * k * x * y
	- 1,				 0	 

end

--%  ft| TRAJECTORY_T0_V0_A1_O1 sn| Slow~convergence~to~a~fixed~point n| #0 d| 0.1 n| #1 d| 0.2 n| #2 d| 20 n| #3 d| 0.05 n| #4 d| 0 n| #5 d| 50000 n| #6 d| 50000 n| #7 d| x n| #8 d| y 
--@@
name = "Conlyapa"
description = "See Model refs in user's guide"
type = "C"
parameters = {"k"}
variables = {"x", "y"}

function f (k, x, y)

	x1 =  - x - y^2 
	y1 = k * x * y 

	return x1, y1

end

function Jf(k, x, y)

	return   
		
	-1,		 - 2 * y,
	k * y,	 k * x	 

end

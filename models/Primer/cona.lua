--%  ft| TRAJECTORY_T0_V0_A1_O1 sn| Oscillatory~convergence~to~a~fixed~point n| #0 d| 1.1 n| #1 d| -1.1 n| #2 d| -0.5 n| #3 d| 0.01 n| #4 d| 0 n| #5 d| 1000 n| #6 d| 1000 n| #7 d| x n| #8 d| y 
--@@
name = "Cona"
description = " See Model refs in user's guide"
type = "C"
parameters = {"a"}
variables = {"x", "y"}

function f (a, x, y)	
	
	x1 =  y - x^2 + 2
	y1 = 2 * a * (x^2 - y^2)

	return x1, y1

end

function Jf(a, x, y)

	return 
  
	-2 * x	, 1,
	4 * a * x	, -4 * a * y

end

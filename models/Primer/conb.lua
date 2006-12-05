--%  ft| TRAJECTORY_T0_V0_A1_O1 sn| Convergence~to~a~fixed~point n| #0 d| 0.1 n| #1 d| 0.1 n| #2 d| -0.35 n| #3 d| -1 n| #4 d| 0.01 n| #5 d| 0 n| #6 d| 3000 n| #7 d| 3000 n| #8 d| x n| #9 d| y 
--@@
name = "Conb"
description = " See Model refs in user's guide"
type = "C"
parameters = {"a", "b"}
variables = {"x", "y"}

function f (a, b, x, y)

	x1 =  x^2 + a * x + x * y ^2
	
	y1 =   b * y^(3/2) - y

   	return x1, y1

end

function Jf(a, b, x, y)

	return 

	a + 2 * x + y^2,	 2 * x * y,
	0,			 - 1 + 3/2 * b * math.sqrt(y)	 

end

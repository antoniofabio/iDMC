--%  ft| TRAJECTORY_T0_V0_A1_O1 sn| Oscillatory~convergence~to~a~fixed~point n| #0 d| 0.2 n| #1 d| 0.1 n| #2 d| 3 n| #3 d| 5 n| #4 d| -5 n| #5 d| -5 n| #6 d| 0.01 n| #7 d| 0 n| #8 d| 1000 n| #9 d| 1000 n| #10 d| x n| #11 d| y 
--@@
name = "Con2d"
description = " See Model refs in user's guide"
type = "C"
parameters = {"a11", "a12", "a21", "a22"}

variables = {"x", "y"}

function f(a11, a12, a21, a22, x, y)

	x1 = a11 * x + a12 * y
	y1 = a21 * x + a22 * y

	return x1, y1

end

function Jf(a11, a12, a21, a22, x, y)

	return  
 
	a11,	 a12,
	a21,	 a22

end

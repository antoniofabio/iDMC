--%  ft| TRAJECTORY_T0_V0_A1_O0 sn| Convergence~to~a~fixed~point n| #0 d| 0.1 n| #1 d| 0.45 n| #2 d| 1 n| #3 d| -0.1 n| #4 d| 0 n| #5 d| 1000 n| #6 d| 1000 n| #7 d| x n| #8 d| y 
--@@
name = "Conc"
description = "See Model refs in user's guide."
type = "D"
parameters = {"a", "b"}
variables = {"x", "y"}

function f(a, b, x, y)

	x1= 2 * b * x + 10
	y1= 2 * a * y^2

	return x1, y1

end


function Jf(a, b, x, y)

return

2 * b,	0,
0,	4 * a * y

end

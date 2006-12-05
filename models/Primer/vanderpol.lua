--%  ft| TRAJECTORY_T0_V0_A1_O1 sn| Convergence~to~a~fixed~point n| #0 d| 0.5 n| #1 d| 0.1 n| #2 d| 5 n| #3 d| -0.1 n| #4 d| 1.5 n| #5 d| 0.01 n| #6 d| 0 n| #7 d| 10000 n| #8 d| 10000 n| #9 d| x n| #10 d| y 
--@@
name = "VanderPol"
description = "See Model refs in user's guide"
type = "C"
parameters = {"k", "mu", "b"}
variables = {"x", "y"}

function f(k, mu, b, x, y)

	x1= k * y + mu * x * (b - y^2)
	y1= - x + mu

	return x1, y1

end


function Jf(k, mu, b, x, y)

return

mu * (b - y^2),	k - 2 * mu * x * y,
-1,			0

end

--%  ft| TRAJECTORY_T0_V0_A1_O0 sn| Chaotic~attractor n| #0 d| 0.46 n| #1 d| 0.37 n| #2 d| 0.937 n| #3 d| 1000 n| #4 d| 10000 n| #5 d| 10000 n| #6 d| x n| #7 d| y 
--%  ft| BIFURCATION_1 sn| Chaos n| #0 d| 0.4 n| #1 d| 0.3 n| #2 d| a n| #3 d| 0 n| #4 d| 2 n| #5 d| -1.1 n| #6 d| 1.1 n| #7 d| 500 n| #8 d| 200 n| #9 d| x 
--@@
name = "Standard"
description = "See Model refs in user's guide"
type = "D"
parameters = {"a"}
variables = {"x", "y"}

function f(a, x, y)

	two_pi = 2 * math.pi    
	x1 = math.mod(x + y + a * (1/two_pi) * math.sin(two_pi * x), 1)
	y1 = y + a * (1/two_pi) * math.sin(two_pi * x)

	return x1, y1

end

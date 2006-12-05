--%  ft| TRAJECTORY_T0_V0_A1_O0 sn| Chaotic~attractor n| #0 d| 1.1 n| #1 d| -1 n| #2 d| 1 n| #3 d| 1000 n| #4 d| 40000 n| #5 d| 40000 n| #6 d| x n| #7 d| y 
--%  ft| TRAJECTORY_T0_V0_A1_O0 sn| Period-6~trajectory n| #0 d| 0 n| #1 d| 1 n| #2 d| 1 n| #3 d| 1000 n| #4 d| 40000 n| #5 d| 40000 n| #6 d| x n| #7 d| y 
--%  ft| BIFURCATION_1 sn| Chaotic~dynamics n| #0 d| 1.1 n| #1 d| -1 n| #2 d| a n| #3 d| -2 n| #4 d| 2 n| #5 d| -5 n| #6 d| 10 n| #7 d| 500 n| #8 d| 500 n| #9 d| x 
--@@
name = "Gingerman"
description = "See Model refs in user's guide"
type = "D"

parameters = {"a"}

variables = {"x", "y",}

function f(a, x, y)

	x1 = 1 - y + math.abs(x)
	y1 = x

	return x1, y1

end

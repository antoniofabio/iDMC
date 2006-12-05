--%  ft| TRAJECTORY_T0_V0_A1_O0 sn| Chaotic~Tinkerbell~attractor n| #0 d| .1 n| #1 d| .1 n| #2 d| 0.9 n| #3 d| -0.6014 n| #4 d| 2 n| #5 d| 0.5 n| #6 d| 1000 n| #7 d| 100000 n| #8 d| 100000 n| #9 d| x n| #10 d| y 
--%  ft| BIFURCATION_1 sn| Route~to~chaos~by~Neimark-Sacker~bifurcations n| #0 d| .1 n| #1 d| .1 n| #2 d| 0.9 n| #3 d| -0.6014 n| #4 d| 2 n| #5 d| c4 n| #6 d| -0.27 n| #7 d| 0.52 n| #8 d| -1.25 n| #9 d| 0.37 n| #10 d| 500 n| #11 d| 100 n| #12 d| x 
--@@
name = "Tinkerbell Map"
description = "See Model refs in user's guide"
type = "D"
parameters = {"c1", "c2", "c3", "c4"}
variables = {"x", "y"}

function f(c1, c2, c3, c4, x, y)

	x1 = x^2 - y^2 + c1 * x + c2 * y	
	y1 = 2 * x * y + c3 * x + c4 * y

	return x1, y1

end

function Jf(c1, c2, c3, c4, x, y)

	return

	2 * x + c1,		-2 * y + c2,
      2 * y + c3,		2 * x + c4

end


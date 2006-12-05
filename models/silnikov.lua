--%  ft| TRAJECTORY_T0_V0_A1_O0 sn| Chaotic~attractor n| #0 d| 0.2 n| #1 d| 0.2 n| #2 d| 0.2 n| #3 d| 0.6 n| #4 d| 0.5 n| #5 d| 0.376 n| #6 d| 1 n| #7 d| 1 n| #8 d| 1000 n| #9 d| 30000 n| #10 d| 30000 n| #11 d| x n| #12 d| y 
--%  ft| BIFURCATION_1 sn| Route~to~chaos n| #0 d| 0.2 n| #1 d| 0.2 n| #2 d| 0.2 n| #3 d| 0.6 n| #4 d| 0.5 n| #5 d| 1 n| #6 d| 1 n| #7 d| c n| #8 d| 0 n| #9 d| 0.48 n| #10 d| -1.25 n| #11 d| 1.25 n| #12 d| 200 n| #13 d| 500 n| #14 d| x 
--@@
name = "Silnikov"
description = "See Model refs in user's guide"
type = "D"
parameters = {"a", "b", "c", "d", "e"}
variables = {"x", "y", "z"}

function f(a, b, c, d, e, x, y, z)

	x1 = a * x - b * (y - z)
	y1 = b * x + a * (y - z) 
	z1 = c * x - d * x^3 + e * z

	return x1, y1, z1

end


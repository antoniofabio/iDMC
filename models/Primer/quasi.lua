--%  ft| TRAJECTORY_T0_V0_A1_O1 sn| Convergence~to~a~quasiperiodic~torus n| #0 d| 0.1 n| #1 d| 0.1 n| #2 d| 0.1 n| #3 d| 2.005 n| #4 d| 3 n| #5 d| 0.2 n| #6 d| 0.2 n| #7 d| 0.02 n| #8 d| 0 n| #9 d| 30000 n| #10 d| 30000 n| #11 d| x n| #12 d| z 
--%  ft| BIFURCATION_1 sn| Quasiperiodic~dynamics n| #0 d| 0.1 n| #1 d| 0.1 n| #2 d| 0.1 n| #3 d| 3 n| #4 d| 0.2 n| #5 d| 0.2 n| #6 d| a n| #7 d| 2 n| #8 d| 2.005 n| #9 d| -2 n| #10 d| 2 n| #11 d| 0 0 1 1 n| #12 d| 10 n| #13 d| 100 n| #14 d| 0.01 n| #15 d| x 
--@@
name = "Quasi"
description = "See Model refs in user's guide"
type = "C"

parameters = {"a", "b", "c", "d"}

variables = {"x", "y", "z"}

function f(a, b, c, d, x, y, z)

	x1 = (a - b) * x - c * y + x * z + x * d * (1.0 - z^2)
	y1 = c * x + (a - b) * y + y * z + y * d * (1.0 - z^2)
	z1 = a * z - x^2 - y^2 - z^2

    return x1, y1, z1

end



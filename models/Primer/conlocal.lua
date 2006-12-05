--%  ft| TRAJECTORY_T0_V0_A1_O1 sn| Convergence~to~a~fixed~point n| #0 d| 0.5 n| #1 d| 1.1 n| #2 d| 2 n| #3 d| 0.01 n| #4 d| 0 n| #5 d| 2000 n| #6 d| 2000 n| #7 d| x n| #8 d| y 
--%  ft| TRAJECTORY_T0_V0_A1_O1 sn| Convergence~to~a~fixed~point,~starting~from~the~neighbourhood~of~an~instable~fixed~point n| #0 d| 2.01 n| #1 d| -2.02 n| #2 d| 2 n| #3 d| 0.01 n| #4 d| 0 n| #5 d| 2000 n| #6 d| 2000 n| #7 d| x n| #8 d| y 
--@@
name = "Conlocal"
description = "See Model refs in user's guide"
type = "C"

parameters = {"a"}

variables = {"x", "y"}


function f(a, x, y)

	x1 = y^2 - 3*x + a
	y2 = x^2 -y^2

	return x1, y2

end


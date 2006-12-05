--%  ft| TRAJECTORY_T1_V0_A1_O1 sn| Convergence~to~a~fixed~point n| #0 d| 0.2 n| #1 d| -0.9 n| #2 d| 0.02 n| #3 d| 0 n| #4 d| 500 n| #5 d| 500 n| #6 d| x 
--@@
name = "Conbif"
description = "See Model refs in user's guide"
type = "C"

parameters = {"mu"}

variables = {"x"}


function f(mu, x)

	x1 = x^3 + x^2 - (2+mu)*x + mu

	return x1

end


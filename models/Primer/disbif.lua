--%  ft| BIFURCATION_1 sn| Period-doubling~route~to~chaos n| #0 d| 0.2 n| #1 d| mu n| #2 d| 0.8 n| #3 d| 2.3 n| #4 d| -2 n| #5 d| 3 n| #6 d| 500 n| #7 d| 100 n| #8 d| x 
--%  ft| TRAJECTORY_T1_V0_A1_O0 sn| Convergence~to~a~3-cycle n| #0 d| 0.2 n| #1 d| 2.015 n| #2 d| 0 n| #3 d| 100 n| #4 d| 100 n| #5 d| x 
--@@
name = "disbif"
description = "See Model refs in user's guide"
type = "D"
parameters = {"mu"}
variables = {"x"}


function f(mu, x)

	x1 = mu + x - x^2
    
	return x1

end

function Jf(mu, x)

	return 1 - 2 * x

end

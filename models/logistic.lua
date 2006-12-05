--%  ft| BIFURCATION_1 sn| Period-doubling~route~to~chaos n| #0 d| .2 n| #1 d| mu n| #2 d| 2.9 n| #3 d| 4 n| #4 d| -0.1 n| #5 d| 1.1 n| #6 d| 500 n| #7 d| 500 n| #8 d| x 
--%  ft| TRAJECTORY_T1_V0_A1_O0 sn| Convergence~to~a~4-cycle n| #0 d| 0.715 n| #1 d| 3.5 n| #2 d| 0 n| #3 d| 75 n| #4 d| 75 n| #5 d| x 
--@@
name = "Logistic"
description = "See Model refs in user's guide"
type = "D"
parameters = {"mu"}
variables = {"x"}

function f(mu, x)

	y = mu*x*(1 - x)

    	return y

end

function Jf(mu, x)

	return mu - 2 * mu * x
end


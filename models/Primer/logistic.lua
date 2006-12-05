--%  ft| TRAJECTORY_T1_V0_A1_O0 sn| Chaotic~trajectory n| #0 d| 0.1 n| #1 d| 3.8 n| #2 d| 0 n| #3 d| 100 n| #4 d| 100 n| #5 d| x 
--%  ft| TRAJECTORY_T1_V0_A1_O0 sn| Convergence~to~a~period-6~cycle n| #0 d| 0.8 n| #1 d| 3.842 n| #2 d| 0 n| #3 d| 150 n| #4 d| 150 n| #5 d| x 
--%  ft| BIFURCATION_1 sn| Period-doubling~route~to~chaos n| #0 d| 0.2 n| #1 d| mu n| #2 d| 2.8 n| #3 d| 4.05 n| #4 d| -0.05 n| #5 d| 1.05 n| #6 d| 500 n| #7 d| 200 n| #8 d| x 
--@@
name = "logistic map"
description = " See Model refs in user's guide"
type = "D"
parameters = {"mu"}
variables = {"x"}

function f(mu, x)

	y = mu * x * (1 - x)

	return y

end

function Jf(mu, x)

	return mu - 2 * mu * x;
end


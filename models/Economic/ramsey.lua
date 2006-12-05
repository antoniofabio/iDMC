--%  ft| TRAJECTORY_T0_V0_A1_O1 sn| c:~0.5~~k:~3~~alph:~0.3~~thet:~1~~rho:~0.03~~g:~0.01~~n:~0.01~~step:~0.05~~ n| #0 d| 0.5 n| #1 d| 3 n| #2 d| 0.3 n| #3 d| 1 n| #4 d| 0.03 n| #5 d| 0.01 n| #6 d| 0.01 n| #7 d| 0.05 n| #8 d| 0 n| #9 d| 5000 n| #10 d| 5000 n| #11 d| k n| #12 d| c 
--%  ft| TRAJECTORY_T0_V1_A1_O1 sn| vari:~9~~step:~0.05~~tran:~0~~iter:~5000~~iter:~5000~~doma:~k~~rang:~c~~ n| #0 d| 9 n| #1 d| 0.05 n| #2 d| 0 n| #3 d| 5000 n| #4 d| 5000 n| #5 d| k n| #6 d| c 
--@@
name = "ramsey" 
description = "See Model refs in user's guide"
type = "C"
parameters = {"alpha", "theta", "rho", "g", "n"}
variables = {"c", "k"}


function f(alpha, theta, rho, g, n, c, k)

	eq1 = (alpha/(theta*k^(1-alpha)) - rho/theta - g)*c
	eq2 = k^(alpha) -c - (n+g)*k

	return eq1, eq2

end


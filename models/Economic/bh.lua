--%  ft| TRAJECTORY_T0_V0_A1_O0 sn| Closed-curve~orbit n| #0 d| 0.1 n| #1 d| 0.2 n| #2 d| 0.3 n| #3 d| 0 n| #4 d| 0 n| #5 d| 0 n| #6 d| 0 n| #7 d| 0 n| #8 d| 0 n| #9 d| 1.2 n| #10 d| 0 n| #11 d| 4 n| #12 d| 0 n| #13 d| 1.1 n| #14 d| 1 n| #15 d| 0 n| #16 d| 30000 n| #17 d| 30000 n| #18 d| x1 n| #19 d| x3 
--%  ft| BIFURCATION_1 sn| x1:~0.1~~x2:~0.2~~x3:~0.3~~n_1:~0~~n_2:~0~~U1:~0~~U2:~0~~g_1:~0~~ n| #0 d| 0.1 n| #1 d| 0.2 n| #2 d| 0.3 n| #3 d| 0 n| #4 d| 0 n| #5 d| 0 n| #6 d| 0 n| #7 d| 0 n| #8 d| 0 n| #9 d| 1.2 n| #10 d| 0 n| #11 d| 4 n| #12 d| 1.15 n| #13 d| 1 n| #14 d| w n| #15 d| 0.25 n| #16 d| 0.6 n| #17 d| -0.1 n| #18 d| 2.3 n| #19 d| 500 n| #20 d| 200 n| #21 d| x1 
--@@
name = "BH"
description = "See Model refs in user's guide"
type = "D"
parameters = {"g_1", "b_1", "g_2", "b_2", "beta", "w", "R", "C"}
variables = {"x1", "x2", "x3", "n_1", "n_2","U1","U2"}

function f( g_1, b_1, g_2, b_2, beta, w, R, C, x1, x2, x3, n_1, n_2, U1, U2)

	e1 = b_1 + g_1 * x1
	e2 = b_2 + g_2 * x1

	U1 = (x1 - R * x2) * (g_1 * x3 + b_1 - R * x2) + w * U1 - C    
	U2 = (x1 - R * x2) * (g_2 * x3 + b_2 - R * x2) + w * U2

	dU = U2 - U1	

	n1 = 1 / ( 1 + math.exp( beta * dU ) )
    	n2 = 1 - n1

	x0 = ( n1 * e1 + n2 * e2 ) / R

    	return x0, x1, x2, n1, n2, U1, U2

end

--%  ft| TRAJECTORY_T1_V0_A1_O1 sn| traj n| #0 d| 2 n| #1 d| 0.2 n| #2 d| 0.3 n| #3 d| 0.01 n| #4 d| 0.01 n| #5 d| 0.04 n| #6 d| 0.05 n| #7 d| 0 n| #8 d| 10000 n| #9 d| 10000 n| #10 d| k 
--%  ft| TRAJECTORY_T1_V1_A1_O1 sn| varic n| #0 d| 8 n| #1 d| 0.05 n| #2 d| 0 n| #3 d| 10000 n| #4 d| 10000 n| #5 d| k 
--%  ft| TRAJECTORY_T1_V1_A1_O1 sn| varalpha n| #0 d| 8 n| #1 d| 0.05 n| #2 d| 0 n| #3 d| 10000 n| #4 d| 10000 n| #5 d| k 
--%  ft| TRAJECTORY_T1_V0_A1_O1 sn| k:~~~s:~~~alph:~~~n:~~~g:~~~delt:~~~step:~~~tran:~~~ n| #0 d|  n| #1 d|  n| #2 d|  n| #3 d|  n| #4 d|  n| #5 d|  n| #6 d|  n| #7 d|  n| #8 d|  n| #9 d|  n| #10 d| k 
--@@
name = "Solow"
description = "See Model refs in user's guide"
type = "C"
parameters = {"s", "alpha", "n", "g", "delta"}
variables = {"k"}

function f(s, alpha, n, g, delta, k)

	k1 = s * k^alpha - (n + g + delta) * k

	return k1

end


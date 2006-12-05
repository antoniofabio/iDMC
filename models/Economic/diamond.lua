--%  ft| COWEB_CO sn| k:~0.01~~alph:~0.3~~n:~0.01~~g:~0.01~~rho:~1~~orde:~1~~tran:~0~~min:~0~~ n| #0 d| 0.01 n| #1 d| 0.3 n| #2 d| 0.01 n| #3 d| 0.01 n| #4 d| 1 n| #5 d| 1 n| #6 d| 0 n| #7 d| 0 n| #8 d| 0.3 n| #9 d| 0 n| #10 d| 0.3 
--%  ft| COWEB_CO sn| k:~0.2~~alph:~0.3~~n:~0.01~~g:~0.01~~rho:~1~~orde:~1~~tran:~0~~min:~0~~ n| #0 d| 0.2 n| #1 d| 0.3 n| #2 d| 0.01 n| #3 d| 0.01 n| #4 d| 1 n| #5 d| 1 n| #6 d| 0 n| #7 d| 0 n| #8 d| 0.3 n| #9 d| 0 n| #10 d| 0.3 
--%  ft| BIFURCATION_1 sn| k:~0.01~~n:~0.01~~g:~0.01~~rho:~1~~Hori:~alpha~~min:~0~~max:~1~~min:~0~~ n| #0 d| 0.01 n| #1 d| 0.01 n| #2 d| 0.01 n| #3 d| 1 n| #4 d| alpha n| #5 d| 0 n| #6 d| 1 n| #7 d| 0 n| #8 d| 0.35 n| #9 d| 200 n| #10 d| 1000 n| #11 d| k 
--%  ft| TRAJECTORY_T1_V0_A1_O0 sn| k:~~~alph:~~~n:~~~g:~~~rho:~~~tran:~~~iter:~~~iter:~~~ n| #0 d|  n| #1 d|  n| #2 d|  n| #3 d|  n| #4 d|  n| #5 d|  n| #6 d|  n| #7 d|  n| #8 d| k 
--%  ft| BIFURCATION_1 sn| k:~0.01~~alph:~0.3~~n:~0.01~~rho:~1~~Hori:~g~~min:~0~~max:~1~~min:~0~~ n| #0 d| 0.01 n| #1 d| 0.3 n| #2 d| 0.01 n| #3 d| 1 n| #4 d| g n| #5 d| 0 n| #6 d| 1 n| #7 d| 0 n| #8 d| 0.35 n| #9 d| 200 n| #10 d| 1000 n| #11 d| k 
--%  ft| BIFURCATION_1 sn| k:~0.01~~alph:~0.3~~n:~0.01~~g:~0.01~~Hori:~rho~~min:~1~~max:~10~~min:~0~~ n| #0 d| 0.01 n| #1 d| 0.3 n| #2 d| 0.01 n| #3 d| 0.01 n| #4 d| rho n| #5 d| 1 n| #6 d| 10 n| #7 d| 0 n| #8 d| 0.35 n| #9 d| 200 n| #10 d| 1000 n| #11 d| k 
--@@
name = "diamond"
description = "See Model refs in user's guide"
type = "D"
parameters = {"alpha", "n", "g", "rho"}
variables = {"k"}

function f (alpha, n, g, rho, k)

	k1 = (k^(alpha))*(1 - alpha)/((1 + n)*(1+g)*(2 + rho)) --2.61

	return k1
end


function Jf(alpha, n, g, rho, k)

return ((1 - alpha)/((1 + n) * (1 + g) * (2 + rho))) * alpha * k^(1 - alpha)

end
